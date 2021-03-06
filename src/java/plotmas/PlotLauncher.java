package plotmas;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.graph.PlotGraph;
import plotmas.helper.PlotFormatter;

/**
 * Used to perform a Java-side setup and execution of a Jason MAS. <br>
 * Sets up a plot graph to draw the plot for each agent and modifies Jason's GUI to display plots graphs and (un)pause
 * a sumulation run.
 * Implements the {@link #run(String[], ImmutableList) run} method to initialize an environment and a model from
 * a list of {@link LauncherAgent parametrized agents} and start the execution of a Jason MAS. <br>
 * <b> Attention: </b> static parameter {@link #ENV_CLASS} needs to be set to the class of your custom environment before executing 
 * {@code run}.
 * 
 * @see plotmas.little_red_hen.RedHenLauncher
 * @author Leonid Berov
 */
public class PlotLauncher extends PlotControlsLauncher {
	protected static Logger logger = Logger.getLogger(PlotLauncher.class.getName());
	public static String DEAULT_FILE_NAME = "launcher.mas2j";

    
    /** 
     * Subclasses need to set ENV_CLASS to the class of their PlotEnvironment implementation, e.g.
     * {@code ENV_CLASS = FarmEnvironment.class;}
     */
	@SuppressWarnings("rawtypes")
	protected static Class ENV_CLASS;
    static Class<PlotAwareAgArch> AG_ARCH_CLASS = PlotAwareAgArch.class;
    static Class<PlotAwareAg> AG_CLASS = PlotAwareAg.class;
    
	protected void createMas2j(Collection<LauncherAgent> agents, String agentFileName) {
		try{
		    PrintWriter writer = new PrintWriter(DEAULT_FILE_NAME, "UTF-8");
		    
		    writer.println("MAS launcher {");
		    writer.println("	environment: " + ENV_CLASS.getName());
		    writer.println("");
		    writer.println("	agents:");
		    
		    for (LauncherAgent agent : agents) {
		    	String line = "		" + agent.name + 
		    			MessageFormat.format(" " + agentFileName + "[beliefs=\"{0}\", goals=\"{1}\"]",
		    								 agent.beliefs,
		    								 agent.goals) +
		    	" agentArchClass " + AG_ARCH_CLASS.getName() + 
		    	" agentClass "+ AG_CLASS.getName() +
		    	";";   
		    	writer.println(line);
		    }
		    
		    writer.println("");
		    writer.println("	aslSourcePath:");
		    writer.println("		\"src/asl\";");
		    writer.println("}");
		    writer.close();
		    
		    logger.info("Generated project config: " + DEAULT_FILE_NAME);
		    
		} catch (IOException e) {
			logger.severe("Couldn't create mas2j file");
		}
	}
	
	/**
	 * Initializes the personality of the AffectiveAgents used to execute character agents. This is a workaround until
	 * we can initialize personality from mas2j files.
	 * @param agents
	 */
	protected void initializePlotAgents(ImmutableList<LauncherAgent> agents) {
		// initialize personalities
		for (LauncherAgent ag: agents) {
			if(ag.personality != null) {
				PlotAwareAg plotAg = (PlotAwareAg) this.getAg(ag.name).getTS().getAg();
				try {
					plotAg.initializePersonality(ag.personality);
				} catch (JasonException e) {
					logger.severe("Failed to initialize mood based on personality: " + ag.personality);
					e.printStackTrace();
				}
				plotAg.initializeMoodMapper();
			}
		}
	}
	
	protected void initzializePlotEnvironment(ImmutableList<LauncherAgent> agents) {
		PlotEnvironment env = (PlotEnvironment) this.env.getUserEnvironment();
		env.initialize(agents);
	}
	
	/**
	 * Has to be executed after initialization is complete because it depends
	 * on PlotEnvironment being already initialized with a plotStartTime.
	 */
	public synchronized void setupPlotLogger() {
        Handler[] hs = Logger.getLogger("").getHandlers(); 
        for (int i = 0; i < hs.length; i++) { 
            Logger.getLogger("").removeHandler(hs[i]); 
        }
        Handler h = PlotFormatter.handler();
        Logger.getLogger("").addHandler(h);
        Logger.getLogger("").setLevel(Level.INFO);
//        Logger.getLogger("").setLevel(Level.FINE);
	}
	
	/**
	 * Creates a mas2j file to prepare execution of the MAS, sets up agents, environment and model and finally starts
	 * the execution of the MAS. The execution is paused if all agents repeat the same action
	 * {@link PlotEnvironment.MAX_REPEATE_NUM} number of times.
	 * <b> Attention: </b> static parameter {@link #ENV_CLASS} needs to be set to the class of your custom environment before executing 
	 * this method.
	 * 
	 * @param args contains the name of the mas2j and potentially {@code -debug} to execute in debug mode
	 * @param agents a list of agent parameters used to initialize mas2j, environment and model
	 * @throws JasonException
	 */
	public void run (String[] args, ImmutableList<LauncherAgent> agents, String agentFileName) throws JasonException  {
		String defArgs[];
		if (ENV_CLASS == null) {
        	throw new RuntimeException("PlotLauncher.ENV_CLASS must be set to the class of your custom"
        			+ " environment before executing this method");
        }
        
        if (args.length < 1) {
        	defArgs = new String[] {PlotLauncher.DEAULT_FILE_NAME};
        }
        else {
        	assert args[0] == "-debug";
        	defArgs = new String[] {PlotLauncher.DEAULT_FILE_NAME, "-debug"};
        }
        
        
		PlotGraph.instantiatePlotListener(agents);
        
		this.createMas2j(agents, agentFileName);
		this.init(defArgs);
		this.create();
        
		this.initzializePlotEnvironment(agents);
		this.setupPlotLogger();
		this.initializePlotAgents(agents);
		
		this.start();
		this.waitEnd();
		this.finish();
	}

	/**
	 * Helper class used to encapsulate all parameters needed to initialise ASL Agents from java code.
	 * This parameters will be used to create a mas2j file required to start a Jason multi agent system. 
	 * @author Leonid Berov
	 */
	public class LauncherAgent {
		public String name;
		public String beliefs;
		public String goals;
		public Personality personality;
		
		public LauncherAgent() {
			this.name = null;
			this.beliefs = "";
			this.goals = "";
			this.personality = null;
		}
		
		public LauncherAgent(String name) {
			this.beliefs = "";
			this.goals = "";
			this.personality = null;
			
			this.name = name;
		}

		public LauncherAgent(String name, Personality personality) {
			this.beliefs = "";
			this.goals = "";
			
			this.name = name;
			this.personality = personality;
		}
		
		public LauncherAgent(String name, Collection<String> beliefs, Collection<String> goals, Personality personality) {
			this.name = name;
			this.beliefs = createLiteralString(beliefs);
			this.goals = createLiteralString(goals);
			this.personality = personality;
		}
		
		/**
		 * Helper function that takes a collection of strings and concatenates them into a list that can be used to 
		 * generate ASL literal lists.
		 */
		private String createLiteralString(Collection<String> literalList) {
			return String.join(",", literalList);
		}
	}
}
