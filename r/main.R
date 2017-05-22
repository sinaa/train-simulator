## includes libs and utils (util.R)
source("util.R")

## Data location
BASE_DATA = "../../../Idea/trainsim/results/"
DATASET_BASE = BASE_DATA

# Output Paths
GRAPHS_DIR = paste("./graphs/", sep="")
dir.create(GRAPHS_DIR, showWarnings = FALSE)

experiments = list.files(path=BASE_DATA, pattern="*.csv.gz", full.names=T, recursive=FALSE)

exName <- function(name){
  name = str_replace_all(name,"-","_")
  name = str_replace(name, ".csv.gz", "")
  return (name)
}

getMaxSpeeds <- function(data){
  return (data %>% filter(type=="TRAIN_MAX_SPEED") %>% mutate(value=as.numeric(value)) %>% select(type,value))
}

getMeanSpeeds <- function(data){
  return (data %>% filter(type=="TRAIN_SPEED") %>% mutate(value=as.numeric(value)) %>% group_by(obj) %>% summarise(avg=mean(value),max=max(value), min=min(value)))
}

exList = list()

for(ex in experiments){
  exList[[exName(basename(ex))]] = loadCSV(basename(ex))
}

