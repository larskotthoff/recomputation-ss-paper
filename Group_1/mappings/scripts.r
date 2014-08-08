library(reshape)
library(ggplot2)

# load data and produce a freq table for each field

maps_data <- read.table("maps_dataframe",header=T)
maps_melt <- melt(maps_data)
attach(maps_melt)
#maps_apply <- tapply(Field,Institution,sum)
maps_table <- table(Field)
maps_table <- rev(sort(maps_table))

# plot histogram of fields
plot(maps_table)

maps_frame <- as.data.frame(table(maps_melt$Field))
maps_frame <- maps_frame[order(-maps_frame$Freq),]
attach(maps_frame)
# maps_subs <- subset(maps_frame,Freq >= 5)
# plot(maps_subs)

# which have complete crossover?
#maps_subs <- subset(maps_frame,maps_table )
barplot(maps_subs)

detach(maps_melt)
attach(maps_frame)

tag_factor <- factor(maps_frame$Var1, levels=maps_frame$Var1)

	
postscript("plot-fieldfreq.eps",horizontal=FALSE,onefile=FALSE,paper="special",width=12,height=6)
p <- ggplot(maps_frame, aes(x=tag_factor, y=Freq)) + geom_bar(stat = "identity",fill="slateblue4",colour="slateblue1") + scale_x_discrete(breaks=NULL,name="Fields") + scale_y_discrete(name="Frequency")
p
dev.off()