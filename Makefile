.PHONY: prepackage test build

prepackage:
	@mvn clean package -DskipTests=true

test: prepackage
	@mvn test --file pom.xml

build: prepackage test
