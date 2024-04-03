.PHONY: test build

test:
	@mvn clean test --file pom.xml
	#@mvn -X clean test --file pom.xml

build:
	@mvn clean package -D maven.test.skip=false
