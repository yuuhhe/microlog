microlog
========

modified for midlet

fork from http://microlog.sourceforge.net/site/

#Basic Usage

	LoggerFactory.getLogger();
	MIDletPropertyConfigurator.configure(midlet);


#Properties

	microlog.appender: ConsoleAppender;HttpAppender
	microlog.appender.HttpAppender.postURL: http://192.168.189.224/log/J2meHandler.ashx
	microlog.formatter: PatternFormatter
	microlog.formatter.PatternFormatter.pattern: %d{ISO8601} [%P] %m %T
	microlog.level: DEBUG
