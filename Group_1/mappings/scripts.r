library(reshape)
library(ggplot2)

# load data and produce a freq table for each field


# load this one for other analysis
maps_data <- read.table("maps_dataframe",header=T)

# use _primary if building CDF to compare filtered attributes
maps_data <- read.table("maps_dataframe_primary",header=T)

maps_melt <- melt(maps_data)
attach(maps_melt)
#maps_apply <- tapply(Field,Institution,sum)
maps_table <- table(Field)
maps_table <- rev(sort(maps_table))

# plot histogram of fields
plot(maps_table)

# for CDF
maps_frame <- as.data.frame(table(maps_melt$Field,maps_melt$Type))

# no CDF
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

# 69 attributes in 1 form
# 29/69 are high-level 


# CDFs
# show frequency distribution of all attributes

# show freq dist of just high-level attributes

maps_frame_cdf <- melt(maps_frame)
maps_frame_cdf <- ddply(maps_frame_cdf, .(variable), transform, ecd=ecdf(value)(value))
maps_frame_cdf <- subset(maps_frame_cdf,value!=0)

postscript("plot-cdf.eps",horizontal=FALSE,onefile=FALSE,paper="special",width=12,height=6)

p <- ggplot(maps_frame_cdf, aes(x=value)) + scale_x_continuous(breaks=1:10,name="Forms") + scale_y_continuous(name="Field frequency") + stat_ecdf(aes(colour=Var2)) + theme(panel.background = element_rect(fill = "transparent",colour = "black")) + scale_colour_manual(name="Field type",labels=c("High level","Sub-attribute"),values=c("#13456f", "#e02828"))

dev.off()