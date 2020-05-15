package gr.uoa.di.kr.yagoextension;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class App {

	static Properties properties = new Properties();
	private final static Logger logger = LogManager.getRootLogger();

	public static void main( String[] args ) throws Exception {

		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF); // suppress Jena's log4j WARN messages
		System.setProperty("org.geotools.referencing.forceXY", "true"); // force (long lat) in geotools
		FileInputStream propertiesFile = new FileInputStream("src/main/resources/execution.properties");
		properties.load(new InputStreamReader(propertiesFile));
		if (!properties.containsKey("mode")) {
			throw new RuntimeException("Please provide an execution mode in the properties file.");
		}

		switch (properties.getProperty("mode")) {
			case "matching":
			  logger.info("Matching Phase");
				MatchingPhase.run(properties);
				break;
			case "generation":
				logger.info("Dataset Generation Mode");
				DatasetGenerator.run(properties);
				break;
			case "topology":
				logger.info("Topological Relations Mode");
				TopologicalRelationsGenerator.run(properties);
				break;
			default:
				throw new RuntimeException("Invalid execution mode");
		}

	}

}
