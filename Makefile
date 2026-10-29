APP_NAME=spring-api-exception-handling

.PHONY: setup dev test build docker clean

setup:
	mvn -B dependency:resolve

dev:
	mvn spring-boot:run

test:
	mvn -B verify

build:
	mvn -B clean package -DskipTests

docker:
	docker compose up --build

clean:
	mvn -B clean
