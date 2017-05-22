#
# Sina's R utils!
#-------------------------------------------------------------------------
# Import necessary libs
libs = c("boot", "moments", "extrafont", "sqldf", "ggplot2", "reshape2", "data.table", "likert", "plyr", "dplyr", "tidyr", "stringr")

new.packages <- libs[!(libs %in% installed.packages()[,"Package"])]
if(length(new.packages)) {
  install.packages(new.packages, repos="http://cran.at.r-project.org")
}
suppressMessages(suppressPackageStartupMessages(suppressWarnings(lapply(libs, require, character.only = TRUE))))

######## STRING utilities ##########

### replace a string
replaceString <- function(string, find, replace) {
  gsub(find, replace, string)
}

### 
isSubString <- function(substring, string) {
  r <- grepl(substring, string)
  return(r)
}

######## HELPERS ##########

getPercentX <- function(number,d=0){
  return(paste(paste(formatC(number, digits=d, format="f"), "\\%",sep="")))
}

printComment <- function(text){
  cat("% ",text, "\n", sep="")
}

printMacro <- function(name,value){
  cat("\\newcommand{\\",name,"}{{",value,"}\\xspace} \n",sep="")
}

niceprint <- function(x, digits = 0, decimal.mark=".", big.mark=",", ...) {
  return (formatC(x, decimal.mark=decimal.mark, big.mark=big.mark, digits = digits, format = "f"))
}

timePrintMins <- function(x){
  mins = floor(x)
  secs = round((x-mins)*60)
  return(sprintf("%d'%d\"",mins,secs))
}

loadCSV <- function(csv){
  if(grepl(".gz$", csv)){
    csv = gzfile(paste(DATASET_BASE, csv, sep=""))
  } else {
    csv = paste(DATASET_BASE, csv, sep="")
  }
  return (read.csv(csv, stringsAsFactors=FALSE))
}


loadTABLE <- function(gzip){
  if(grepl(".gz$", gzip)){
    gzip = gzfile(paste(DATASET_BASE, gzip, sep=""))
  } else {
    gzip = paste(DATASET_BASE, gzip, sep="")
  }
  return (read.table(gzip, header=T, stringsAsFactors=FALSE))
}

wrapTex <- function(tex, wrapper_start="\\textbf{", wrapper_end="}", condition=T){
  
  if(condition){
    return (paste(wrapper_start, tex, wrapper_end, sep=""))
  }
  
  return (tex)
}

######## PLOT HELPERS #########

savePlot <- function(FUN, name, w=14, h=1){
  pdfname = paste(GRAPHS_DIR, name, ".pdf", sep="")
  
  unlink(pdfname)
  pdf(file = pdfname, width=w, height=h, family="Helvetica", fonts = "Helvetica")
  
  FUN()
  
  dev.off()
  
  # Embed the fonts
  gs_path = ""
  if(.Platform$OS.type != "unix") {
    gs_path = "C:/Program Files/gs/gs9.18/bin/gswin64c.exe"
    if(!file.exists(gs_path))
      gs_path = "C:/Program Files/gs/gs9.19/bin/gswin64c.exe"
    if(!file.exists(gs_path))
      gs_path = "C:/Program Files/gs/gs9.20/bin/gswin64c.exe"
    if(!file.exists(gs_path))
      gs_path = "C:/Program Files/gs/gs9.21/bin/gswin64c.exe"	  
    if(!file.exists(gs_path))
      gs_path = "C:/Program Files/gs/gs9.22/bin/gswin64c.exe"
  } else {
    gs_path = "/usr/bin/gs"
  }
  
  if(!file.exists(gs_path))
    return(TRUE);
  
  Sys.setenv(R_GSCMD = gs_path)
  
  embed_fonts(pdfname, options="-dSubsetFonts=true -dEmbedAllFonts=true -dCompatibilityLevel=1.4 -dPDFSETTINGS=/prepress -dMaxSubsetPct=100")
}

varName <- function(var){
  return(deparse(substitute(var)))
}
