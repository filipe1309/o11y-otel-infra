// Copyright The OpenTelemetry Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Sample contains a simple http server that exports to the OpenTelemetry agent.

package main

import (
	"context"
	"fmt"
	"log/slog"
	"math/rand"
	"net/http"
	"os"
	"time"

	"go.opentelemetry.io/contrib/bridges/otelslog"
	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/baggage"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/log/global"
	"go.opentelemetry.io/otel/metric"
	"go.opentelemetry.io/otel/propagation"
	sdkmetric "go.opentelemetry.io/otel/sdk/metric"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.21.0"
	"go.opentelemetry.io/otel/trace"

	"go.opentelemetry.io/otel/exporters/otlp/otlplog/otlploggrpc"
	"go.opentelemetry.io/otel/sdk/log"
)

var rng = rand.New(rand.NewSource(time.Now().UnixNano()))

// Initializes an OTLP exporter, configuring the corresponding trace, metric, and log providers.
func initProvider() func() {
	ctx := context.Background()

	otelAgentAddr, ok := os.LookupEnv("OTEL_EXPORTER_OTLP_ENDPOINT")
	if !ok {
		otelAgentAddr = "0.0.0.0:4317"
	}

	res, err := resource.New(ctx,
		resource.WithFromEnv(),
		resource.WithProcess(),
		resource.WithTelemetrySDK(),
		resource.WithHost(),
		resource.WithAttributes(
			// the service name used to display traces in backends
			semconv.ServiceNameKey.String("ms-server"),
		),
	)
	handleErr(err, "failed to create resource")

	// Metrics

	// Initialize metric exporter
	metricExp, err := otlpmetricgrpc.New(ctx,
		otlpmetricgrpc.WithInsecure(),
		otlpmetricgrpc.WithEndpoint(otelAgentAddr))
	handleErr(err, "Failed to create the collector metric exporter")

	// Initialize metric provider
	meterProvider := sdkmetric.NewMeterProvider(
		sdkmetric.WithReader(sdkmetric.NewPeriodicReader(metricExp,
			sdkmetric.WithInterval(2*time.Second))),
	)
	otel.SetMeterProvider(meterProvider)

	// Traces

	traceClient := otlptracegrpc.NewClient(
		otlptracegrpc.WithInsecure(),
		otlptracegrpc.WithEndpoint(otelAgentAddr))
	traceExp, err := otlptrace.New(ctx, traceClient)
	handleErr(err, "Failed to create the collector trace exporter")

	bsp := sdktrace.NewBatchSpanProcessor(traceExp)
	tracerProvider := sdktrace.NewTracerProvider(
		sdktrace.WithSampler(sdktrace.AlwaysSample()),
		sdktrace.WithResource(res),
		sdktrace.WithSpanProcessor(bsp),
	)

	// set global propagator to tracecontext (the default is no-op).
	otel.SetTextMapPropagator(propagation.NewCompositeTextMapPropagator(propagation.TraceContext{}, propagation.Baggage{}))
	otel.SetTracerProvider(tracerProvider)

	// Logs

	// Create a logger provider.
	// You can pass this instance directly when creating bridges.
	loggerProvider, err := newLoggerProvider(ctx, res)
	if err != nil {
		panic(err)
	}

	// Handle shutdown properly so nothing leaks.
	// defer func() {
	// 	if err := loggerProvider.Shutdown(ctx); err != nil {
	// 		fmt.Println(err)
	// 	}
	// }()

	// Register as global logger provider so that it can be accessed global.LoggerProvider.
	// Most log bridges use the global logger provider as default.
	// If the global logger provider is not set then a no-op implementation
	// is used, which fails to generate data.
	global.SetLoggerProvider(loggerProvider)

	// Create an otelslog logger and set it as the default logger for the slog package.
	slog.SetDefault(otelslog.NewLogger("ms-server", otelslog.WithLoggerProvider(loggerProvider)))

	return func() {
		cxt, cancel := context.WithTimeout(ctx, time.Second)
		defer cancel()
		if err := traceExp.Shutdown(cxt); err != nil {
			otel.Handle(err)
		}
		// shutdown metric provider
		if err := meterProvider.Shutdown(cxt); err != nil {
			otel.Handle(err)
		}

		if err := loggerProvider.Shutdown(ctx); err != nil {
			otel.Handle(err)
		}
	}
}

func handleErr(err error, message string) {
	if err != nil {
		slog.Error(fmt.Sprintf("%s: %v", message, err))
		os.Exit(1)
	}
}

func main() {
	shutdown := initProvider()
	defer shutdown()

	slog.Info("Application started", "version", "1.0.0")

	meter := otel.Meter("ms-server-meter")
	serverAttribute := attribute.String("server-attribute", "foo")
	commonLabels := []attribute.KeyValue{serverAttribute}
	requestCount, _ := meter.Int64Counter(
		"ms_server/request_counts",
		metric.WithDescription("The number of requests received"),
	)

	// create a handler wrapped in OpenTelemetry instrumentation
	handler := http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
		slog.Info("Received request", "method", req.Method, "url", req.URL.String())

		//  random sleep to simulate latency
		var sleep int64

		switch modulus := time.Now().Unix() % 5; modulus {
		case 0:
			sleep = rng.Int63n(2000)
		case 1:
			sleep = rng.Int63n(15)
		case 2:
			sleep = rng.Int63n(917)
		case 3:
			sleep = rng.Int63n(87)
		case 4:
			sleep = rng.Int63n(1173)
		}
		time.Sleep(time.Duration(sleep) * time.Millisecond)
		ctx := req.Context()
		requestCount.Add(ctx, 1, metric.WithAttributes(commonLabels...))
		span := trace.SpanFromContext(ctx)
		bag := baggage.FromContext(ctx)

		var baggageAttributes []attribute.KeyValue
		baggageAttributes = append(baggageAttributes, serverAttribute)
		for _, member := range bag.Members() {
			baggageAttributes = append(baggageAttributes, attribute.String("baggage key:"+member.Key(), member.Value()))
		}
		span.SetAttributes(baggageAttributes...)

		if _, err := w.Write([]byte("Hello World")); err != nil {
			slog.Error("Failed to write response", "error", err)
			http.Error(w, "write operation failed.", http.StatusInternalServerError)
			return
		}

	})

	mux := http.NewServeMux()
	mux.Handle("/hello", otelhttp.NewHandler(handler, "/hello"))
	server := &http.Server{
		Addr:    ":7080",
		Handler: mux,
	}
	if err := server.ListenAndServe(); err != http.ErrServerClosed {
		handleErr(err, "server failed to serve")
	}
}

func newLoggerProvider(ctx context.Context, res *resource.Resource) (*log.LoggerProvider, error) {
	otelAgentAddr, ok := os.LookupEnv("OTEL_EXPORTER_OTLP_ENDPOINT")
	if !ok {
		otelAgentAddr = "0.0.0.0:4317"
	}

	exporter, err := otlploggrpc.New(ctx,
		otlploggrpc.WithInsecure(),
		otlploggrpc.WithEndpoint(otelAgentAddr))
	if err != nil {
		return nil, err
	}
	processor := log.NewBatchProcessor(exporter)
	provider := log.NewLoggerProvider(
		log.WithResource(res),
		log.WithProcessor(processor),
	)
	return provider, nil
}
