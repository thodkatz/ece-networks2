SRC = src/*.java src/applications/*.java

$(shell mkdir -p classes logs media)
CLASSES = classes
MAIN    = UserApplication

all: build run

build: $(SRC)
	javac -cp lib/htmlunit.jar -d $(CLASSES) $^

run:
	@java -cp $(CLASSES):lib/htmlunit.jar $(MAIN)

.PHONY: clean

clean:
	rm -rf $(CLASSES)/*

clean_logs:
	rm -f media/*.jpg logs/*.txt