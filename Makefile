SHELL := /bin/bash

.PHONY: help up down restart logs logs-follow ps clean clean-all health status \
        start stop build rebuild validate config test-services \
        jaeger prometheus grafana opensearch otel-collector otel-viewer \
        example-go-basic example-typescript \
        clean-examples clean-volumes backup-config restore-config \
        monitoring-only prune pull update quick-start

# Default target
.DEFAULT_GOAL := help

# Color output
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

# Project variables
PROJECT_NAME := o11y-otel-infra
COMPOSE_FILE := docker-compose.yaml
DOCKER_COMPOSE := docker compose
BACKUP_DIR := backups

##@ General

help: ## Display this help message
	@echo -e "$(BLUE)$(PROJECT_NAME) - Observability & OpenTelemetry Infrastructure$(NC)"
	@echo -e ""
	@awk 'BEGIN {FS = ":.*##"; printf "Usage:\n  make $(GREEN)<target>$(NC)\n"} /^[a-zA-Z_0-9%%-]+:.*?##/ { printf "  $(GREEN)%-25s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(BLUE)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Stack Management

up: ## Start all services
	@echo -e "$(GREEN)Starting observability stack...$(NC)"
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) up -d --remove-orphans
	@echo -e "$(GREEN)Stack started successfully!$(NC)"
	@echo -e "$(YELLOW)Run 'make health' to check service status$(NC)"

down: ## Stop all services
	@echo -e "$(YELLOW)Stopping observability stack...$(NC)"
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) down
	@echo -e "$(GREEN)Stack stopped successfully!$(NC)"

restart: down up ## Restart all services

start: ## Start stopped services (without recreating)
	@echo -e "$(GREEN)Starting services...$(NC)"
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) start

stop: ## Stop services without removing containers
	@echo -e "$(YELLOW)Stopping services...$(NC)"
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) stop

build: ## Build or rebuild services
	@echo -e "$(GREEN)Building services...$(NC)"
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) build

rebuild: clean build up ## Clean, rebuild and start services

##@ Monitoring

ps: ## List running containers
	@$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) ps

status: ps ## Alias for ps

logs: ## Show logs from all services
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) logs

logs-follow: ## Follow logs from all services
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) logs -f

logs-%: ## Show logs for a specific service (e.g., make logs-jaeger)
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) logs -f $*

health: ## Check health status of all services
	@echo -e "$(BLUE)Checking service health...$(NC)"
	@echo -e ""
	@echo -e "$(GREEN)Jaeger:$(NC) http://localhost:16686"
	@curl -sf http://localhost:16686 > /dev/null && echo "  ✓ Running" || echo "  $(RED)✗ Not responding$(NC)"
	@echo -e "$(GREEN)Prometheus:$(NC) http://localhost:9090"
	@curl -sf http://localhost:9090/-/healthy > /dev/null && echo "  ✓ Running" || echo "  $(RED)✗ Not responding$(NC)"
	@echo -e "$(GREEN)Grafana:$(NC) http://localhost:3000"
	@curl -sf http://localhost:3000/api/health > /dev/null && echo "  ✓ Running" || echo "  $(RED)✗ Not responding$(NC)"
	@echo -e "$(GREEN)OpenSearch:$(NC) http://localhost:9200"
	@curl -sf http://localhost:9200/_cluster/health > /dev/null && echo "  ✓ Running" || echo "  $(RED)✗ Not responding$(NC)"
	@echo -e "$(GREEN)OTEL Collector:$(NC) http://localhost:8888"
	@curl -sf http://localhost:8888/metrics > /dev/null && echo "  ✓ Running" || echo "  $(RED)✗ Not responding$(NC)"
	@echo -e "$(GREEN)OTEL Viewer:$(NC) http://localhost:8000"
	@curl -sf http://localhost:8000 > /dev/null && echo "  ✓ Running" || echo "  $(RED)✗ Not responding$(NC)"

test-services: health ## Alias for health check

##@ Individual Services

jaeger: ## Open Jaeger UI in browser
	@echo -e "$(GREEN)Opening Jaeger UI...$(NC)"
	@open http://localhost:16686 || xdg-open http://localhost:16686 || echo "$(YELLOW)Please open http://localhost:16686 manually$(NC)"

prometheus: ## Open Prometheus UI in browser
	@echo -e "$(GREEN)Opening Prometheus UI...$(NC)"
	@open http://localhost:9090 || xdg-open http://localhost:9090 || echo "$(YELLOW)Please open http://localhost:9090 manually$(NC)"

grafana: ## Open Grafana UI in browser
	@echo -e "$(GREEN)Opening Grafana UI...$(NC)"
	@open http://localhost:3000 || xdg-open http://localhost:3000 || echo "$(YELLOW)Please open http://localhost:3000 manually$(NC)"

opensearch: ## Check OpenSearch cluster health
	@echo -e "$(GREEN)OpenSearch Cluster Health:$(NC)"
	@curl -s http://localhost:9200/_cluster/health?pretty || echo "$(RED)OpenSearch not responding$(NC)"

otel-collector: ## Check OTEL Collector metrics
	@echo -e "$(GREEN)OTEL Collector Metrics:$(NC)"
	@curl -s http://localhost:8888/metrics | head -20 || echo "$(RED)OTEL Collector not responding$(NC)"

otel-viewer: ## Open OTEL Viewer (otel-front) UI in browser
	@echo -e "$(GREEN)Opening OTEL Viewer UI...$(NC)"
	@open http://localhost:8000 || xdg-open http://localhost:8000 || echo "$(YELLOW)Please open http://localhost:8000 manually$(NC)"

monitoring-only: ## Start only monitoring services (Prometheus, Grafana)
	@echo -e "$(GREEN)Starting monitoring services only...$(NC)"
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) up -d prometheus grafana

##@ Configuration

validate: ## Validate docker-compose configuration
	@echo -e "$(GREEN)Validating docker-compose configuration...$(NC)"
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) config > /dev/null
	@echo -e "$(GREEN)Configuration is valid!$(NC)"

config: ## Show docker-compose configuration
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) config

backup-config: ## Backup configuration files
	@echo -e "$(GREEN)Backing up configuration files...$(NC)"
	@mkdir -p $(BACKUP_DIR)
	@tar -czf $(BACKUP_DIR)/config-backup-$$(date +%Y%m%d-%H%M%S).tar.gz \
		docker-compose.yaml \
		otel-collector/otelcol-config.yaml \
		prometheus/config.yaml \
		jaeger/config.yml \
		grafana/grafana.ini \
		grafana/provisioning/datasources/ \
		opensearch/Dockerfile
	@echo -e "$(GREEN)Configuration backed up to $(BACKUP_DIR)/$(NC)"

restore-config: ## List available backups
	@echo -e "$(BLUE)Available backups:$(NC)"
	@ls -lh $(BACKUP_DIR)/ 2>/dev/null || echo "$(YELLOW)No backups found$(NC)"

##@ Examples

example-go-basic: ## Run Go basic client-server example
	@echo -e "$(GREEN)Starting Go basic example...$(NC)"
	@cd examples/go/basic && \
		$(DOCKER_COMPOSE) -f ../../../docker-compose.yaml -f ./docker-compose.yml up -d --build --force-recreate
	@echo -e "$(GREEN)Go basic example started!$(NC)"
	@echo -e "$(YELLOW)View traces at http://localhost:16686$(NC)"

example-typescript: ## Run TypeScript example
	@echo -e "$(GREEN)Starting TypeScript example...$(NC)"
	@cd examples/typescript && \
		$(DOCKER_COMPOSE) -f ../../docker-compose.yaml -f ./docker-compose.yaml up -d --build --force-recreate
	@echo -e "$(GREEN)TypeScript example started!$(NC)"
	@echo -e "$(YELLOW)View traces at http://localhost:16686$(NC)"

clean-examples: ## Stop and remove all example containers
	@echo -e "$(YELLOW)Cleaning up examples...$(NC)"
	@cd examples/go/basic && $(DOCKER_COMPOSE) -f ../../../docker-compose.yaml -f ./docker-compose.yml down 2>/dev/null || true
	@cd examples/typescript && $(DOCKER_COMPOSE) -f ../../docker-compose.yaml -f ./docker-compose.yaml down 2>/dev/null || true
	@echo -e "$(GREEN)Examples cleaned up!$(NC)"

##@ Cleanup

clean: ## Remove containers and networks (keeps volumes)
	@echo -e "$(YELLOW)Removing containers and networks...$(NC)"
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) down
	@echo -e "$(GREEN)Cleanup complete!$(NC)"

clean-volumes: ## Remove containers, networks, and volumes (WARNING: deletes data)
	@echo -e "$(RED)WARNING: This will delete all data including Prometheus metrics!$(NC)"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) down -v; \
		echo "$(GREEN)Volumes removed!$(NC)"; \
	else \
		echo "$(YELLOW)Cancelled.$(NC)"; \
	fi

clean-all: clean-examples clean-volumes ## Remove everything including examples and volumes

prune: ## Remove unused Docker resources (images, containers, networks)
	@echo -e "$(YELLOW)Pruning Docker resources...$(NC)"
	@docker system prune -f
	@echo -e "$(GREEN)Docker resources pruned!$(NC)"

##@ Development

shell-%: ## Open shell in a specific service (e.g., make shell-prometheus)
	@$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) exec $* sh || \
		$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) exec $* bash

inspect-%: ## Inspect a specific service configuration (e.g., make inspect-jaeger)
	@$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) config --services | grep -q "^$*$$" && \
		docker inspect $$($(DOCKER_COMPOSE) -f $(COMPOSE_FILE) ps -q $*) | (jq '.[0]' 2>/dev/null || python3 -m json.tool) || \
		echo "$(RED)Service $* not found$(NC)"

pull: ## Pull latest images
	@echo -e "$(GREEN)Pulling latest images...$(NC)"
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) pull

update: pull restart ## Update all services to latest versions

##@ Quick Start

quick-start: validate up health ## Quick start: validate, start services, and check health
	@echo -e ""
	@echo -e "$(GREEN)╔════════════════════════════════════════════════╗$(NC)"
	@echo -e "$(GREEN)║  Observability Stack is Ready!                 ║$(NC)"
	@echo -e "$(GREEN)╚════════════════════════════════════════════════╝$(NC)"
	@echo -e ""
	@echo -e "$(BLUE)Access your services:$(NC)"
	@echo -e "  • Jaeger:     http://localhost:16686"
	@echo -e "  • Prometheus: http://localhost:9090"
	@echo -e "  • Grafana:    http://localhost:3000"
	@echo -e "  • OpenSearch: http://localhost:9200"
	@echo -e "  • OTEL Viewer: http://localhost:8000"
	@echo -e ""
	@echo -e "$(YELLOW)Next steps:$(NC)"
	@echo -e "  • Run 'make example-go-basic' to test with Go example"
	@echo -e "  • Run 'make logs-follow' to watch all logs"
	@echo -e ""

install: quick-start ## Alias for quick-start

info: ## Show project information
	@echo -e "$(BLUE)Project Information$(NC)"
	@echo -e "  Name:           $(PROJECT_NAME)"
	@echo -e "  Compose File:   $(COMPOSE_FILE)"
	@echo -e "  Docker Version: $$(docker --version)"
	@echo -e "  Compose Version: $$($(DOCKER_COMPOSE) --version)"
	@echo -e ""
	@echo -e "$(BLUE)Available Services:$(NC)"
	@$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) config --services | sed 's/^/  • /'
	@echo -e ""
	@echo -e "$(BLUE)Examples:$(NC)"
	@echo -e "  • Go Basic"
	@echo -e "  • TypeScript"
