.PHONY: build
build:
	@mvn clean package -D maven.test.skip=false