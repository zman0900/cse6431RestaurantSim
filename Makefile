# Java programs
JAVAC = javac
JAR = jar
JAVA = java

# Java compiler flags
JAVAFLAGS = -g

# Source directory
SOURCE = src

# Build directory
BUILD = build

# Output jar
OUT = RestaurantSim.jar

# Main class
MAINCLASS = RestaurantSim
MAINPACKAGE = com.cse6431
MAINFILE = com/cse6431/RestaurantSim.java

# The first target is the one that is executed when you invoke
# "make". 

all : archive
	$(MAKE) clean-build

build :
	mkdir -p $(BUILD)
	$(JAVAC) $(JAVAFLAGS) -sourcepath $(SOURCE) -cp $(BUILD) -d $(BUILD) $(SOURCE)/$(MAINFILE)

archive : build
	$(JAR) cvfe $(OUT) $(MAINPACKAGE).$(MAINCLASS) -C $(BUILD) .

clean : clean-build clean-jar
	
clean-build :
	rm -rf $(BUILD)

clean-jar :
	rm -f $(OUT)
