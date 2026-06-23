package ni.edu.uam.bfavocabulario.run;

import org.openxava.util.*;

/**
 * Execute this class to start the application.
 */

public class bfavocabulario {

	public static void main(String[] args) throws Exception {
		/*DBServer.start("bfavocabulario-db"); // To use your own database comment this line and configure src/main/webapp/META-INF/context.xml*/
		AppServer.run("bfavocabulario"); // Use AppServer.run("") to run in root context
	}

}
