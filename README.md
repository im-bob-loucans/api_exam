# SCOIR Technical Interview for Back-End Engineers
This repo contains an exercise intended for Back-End Engineers.

## Instructions
1. Fork this repo.
1. Using technology of your choice, complete [the assignment](./Assignment.md).
1. Update this README with
    * a `How-To` section containing any instructions needed to execute your program.
    * an `Assumptions` section containing documentation on any assumptions made while interpreting the requirements.
1. Before the deadline, submit a pull request with your solution.

## Expectations
1. Please take no more than 8 hours to work on this exercise. Complete as much as possible and then submit your solution.
1. This exercise is meant to showcase how you work. With consideration to the time limit, do your best to treat it like a production system.

## How-To
- NOTE: These instructions are for Mac only 

1. Install a JRE:  
   - Download the OpenJDK JRE (11 or better) and install: https://adoptopenjdk.net/ - you will most likely need admin privileges to do this - check with your IT team.

2. Clone a local copy of the repo - follow GitHub instructions for setting up a client

3. Open a terminal window and run the following commands
    ```
    cd <full path project root - location where you clones the repo> 
    
    mkdir -p /Users/<your user folder>/csvtojson/input
    mkdir -p /Users/<your user folder>/csvtojson/output
    mkdir -p /Users/<your user folder>/csvtojson/error
    
    ./csvToJson.sh -i <full path to input dir> -e <full path to error dir> -o <full path to output dir>
    ```
   
   - Application should start successfully - look for this message in the logs:
   ``` 
   "polling inputPath for events"
   ```
    
   - Copy a csv file to the input directory and monitor the output logs for processing status - - look for this message in the logs:
   ``` 
   "processing new csv file on thread, filename...."
   ```
   
## Assumptions

Current working assumptions to be able to make progress:

1. Input files are UTF-8 encoded
2. Input files conform to CSV RFC4180 Standard for quoted values
3. Input files use only comma delimiters
4. All columns are provided in the order specified in the requirements
5. A command line tool is what the user wants
6. Java is an acceptable technology
7. Installing a JRE is something users will be capable of doing
8. MacOS is the target platform 
9. Using a dist folder with a committed binary is fine for distribution
10. Users will copy files in and they will be small enough to compete in a single FS event cycle
11. Users will not edit or otherwise manage files in the input folder
12. Users will not edit or other manage files in the output folder
13. Parallel file processing is needed
14. Security considerations will be discussed later
     - Files contain PII, logs also
      - Moving of files possible over network
15. files could be big enough to warrant a complex row by row processing approach
16. Logging to std out is ok
17. No dev norms for the repo - commit templates, code style, coverage, etc….
18. Input location exist prior to running
19. Users have backups of their source files
20. Users will enter different locations - error and input folders cause a loop



