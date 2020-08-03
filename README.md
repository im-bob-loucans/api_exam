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
1. Install a JRE:  
   - Download the OpenJDK 13 JRE Runtime and install: https://adoptopenjdk.net/

2. Clone a local copy of the repo

3. Open a terminal window and run the following commands
    ```
    cd <project root>
    ./csvToJson.sh -i <full path to input dir> -e <full path to error dir> -o <full path to output dir>
    ```
   
   application will start,  add a csv file to the input directory and monitor the logs for processing
   
## Assumptions
1. Input files are UTF-8 encoded
2. Input files conform to CSV RFC4180 Standard for quoted values
3. Input files use only comma delimiters
3. All columns are provided in specified order in the CSV...

