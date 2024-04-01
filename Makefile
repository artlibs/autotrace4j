.PHONY: test build

test:
	@mvn -X clean test --file pom.xml

build:
	@mvn clean package -D maven.test.skip=false
