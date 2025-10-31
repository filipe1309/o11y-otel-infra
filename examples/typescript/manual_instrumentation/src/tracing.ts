import { BatchSpanProcessor, ConsoleSpanExporter } from "@opentelemetry/sdk-trace-base";
import { Resource } from "@opentelemetry/resources";
import { SemanticResourceAttributes } from "@opentelemetry/semantic-conventions";
import { NodeTracerProvider } from "@opentelemetry/sdk-trace-node";
import * as opentelemetry from "@opentelemetry/sdk-node";
import { getNodeAutoInstrumentations } from "@opentelemetry/auto-instrumentations-node";
import { OTLPTraceExporter } from "@opentelemetry/exporter-trace-otlp-proto";
import { OTLPMetricExporter } from "@opentelemetry/exporter-metrics-otlp-proto";
import { PeriodicExportingMetricReader } from "@opentelemetry/sdk-metrics";

const otelExporterOtlpEndpoint: string = process.env?.OTEL_EXPORTER_OTLP_ENDPOINT!;

const resource = Resource.default().merge(
    new Resource({
        [SemanticResourceAttributes.SERVICE_NAME]:'test-app',
        [SemanticResourceAttributes.SERVICE_VERSION]:'0.1.0'
    }));


const sdk = new opentelemetry.NodeSDK({
    traceExporter: new OTLPTraceExporter({
        url: `${otelExporterOtlpEndpoint}/v1/traces`,
        headers: {},
    }),
    metricReader: new PeriodicExportingMetricReader({
        exporter: new OTLPMetricExporter({
        url: `${otelExporterOtlpEndpoint}/v1/metrics`, 
        headers: {}, 
        }),
    }),
    instrumentations: [getNodeAutoInstrumentations()],
});

sdk.start();    

const provider = new NodeTracerProvider({resource});

const exporter = new ConsoleSpanExporter();

const processor = new BatchSpanProcessor(exporter);

provider.addSpanProcessor(processor);

provider.register();