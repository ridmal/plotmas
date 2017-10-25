package plotmas.little_red_hen;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.AffectiveTransitionSystem;
import jason.asSemantics.Personality;
import plotmas.PlotLauncher;
import plotmas.graph.PlotGraph;
import plotmas.storyworld.StoryworldAgent;


public class RedHenLauncher extends PlotLauncher {

	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!"); 
        
        ImmutableList<StoryworldAgent> agents = ImmutableList.of(
							new StoryworldAgent("hen",							  // works with Mood.MAX_DECAY_TIME = 50 and MAX_UPDATE_TIME = 5
									new Personality(0,  1, 0.7,  0.3, 0.0)    //punishment
//									new Personality(0,  1, 0.7,  0.3,  -1)    //low neurot --> no punishment
//									new Personality(0,  1, 0.7,  0.7,  -1)    //high aggrea --> sharing
									
//									new Personality(0, -1, 0.7,  0.3, 0.0)    //low consc --> no plot (graph making brakes?!)
									
//									new Personality(0,  1, 0,    0.7, 0.0)    //low extra --> no help requests, no punishment
							),
							new StoryworldAgent("dog",
									new Personality(0, -1, 0, -0.7, -0.8)
//									new Personality(0, 1, 0, -0.7, -0.8)	// doggie helps hen v1
							),
							new StoryworldAgent("cow",
									new Personality(0, -1, 0, -0.7, -0.8)
//									new Personality(0, 1, 0, -0.7, -0.8)	// cow helps hen v1
							),
							new StoryworldAgent("pig",
									new Personality(0, -1, 0, -0.7, -0.8)
							)
						);
        
        PlotGraph.instantiatePlotListener(agents);
        
		runner = new RedHenLauncher();
		runner.createMas2j(agents);
        runner.init(args);
        runner.create();
        
        runner.initializeAgents(agents);
        runner.initzializeEnvironment(agents);
        
        runner.start();
        runner.waitEnd();
        runner.finish();
        
	}
}