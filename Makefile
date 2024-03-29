.PHONY: test build

test:
	@mvn clean test

build:
	@mvn clean package -D maven.test.skip=false
