common_targets := $(wildcard dev/leonardini/worth/data/*.java) $(wildcard dev/leonardini/worth/networking/*.java)
client_targets := $(wildcard dev/leonardini/worth/client/*.java) $(common_targets)
server_targets := $(wildcard dev/leonardini/worth/server/*.java) $(common_targets)

.PHONY: clean test all 

all: ClientWORTH.jar ServerWORTH.jar

ClientWORTH.jar: $(client_targets)
	@echo "Compiling client sources..."
	@javac $(client_targets)
	@echo "Creating executable jar..."
	@jar cmf CLIENT_MANIFEST.MF ClientWORTH.jar dev
	@zip -d ClientWORTH.jar dev/leonardini/worth/server/\* &> /dev/null

ServerWORTH.jar: $(server_targets)
	@echo "Compiling server sources..."
	@javac $(server_targets)
	@echo "Creating executable jar..."
	@jar cmf SERVER_MANIFEST.MF ServerWORTH.jar dev
	@zip -d ServerWORTH.jar dev/leonardini/worth/client/\* &> /dev/null

clean:
	@echo "Deleting .class files..."
	@find . -type f -name "*.class" -delete
	@echo "Deleting .jar files..."
	@rm -rf *.jar
