common_targets := $(wildcard dev/leonardini/worth/data/*.java) $(wildcard dev/leonardini/worth/networking/*.java)
client_targets := $(wildcard dev/leonardini/worth/client/*.java) $(common_targets)
server_targets := $(wildcard dev/leonardini/worth/server/*.java) $(common_targets)
all_targets := $(common_targets) $(client_targets) $(server_targets)

.PHONY: clean test all

all: ClientWORTH.jar

ClientWORTH.jar: $(all_targets)
	javac $(all_targets)
	jar cmf CLIENT_MANIFEST.MF ClientWORTH.jar dev
	zip -d ClientWORTH.jar dev/leonardini/worth/server/\*

clean:
	rm -rf dev/leonardini/worth/*.class
	rm -rf *.jar
