// PARAMETERS
emo_threshold(10).

// EMOTIONS
emotion(anger(_)).
emotion(happiness(_)).

anger(0)[target([])].
happiness(0)[target([])].


@reset_emotions[atomic]
+!reset_emotion(Em1, Em2) : emotion(Em1) & emotion(Em2) <-
	-Em1;
	+Em2[target([])].


// HAPPINESS SPECIFIC

/* happiness causes reward desire */
+happiness(X)[target(L)] : emo_threshold(X) <-
	!rewarded(L).
	
/* begin declarative goal  (p. 174; Bordini,2007)*/
+!rewarded(L) : rewarded(L) <- true.
	
+!rewarded(L) : true <- 
	.send(L, tell, praised(self));
	+rewarded(L);
	?rewarded(L).
	
+rewarded(L) : true <- 
	!reset_emotion(happiness(_), happiness(0));
	-rewarded(L);	
	.succeed_goal(rewarded(L)).

//     +backtracking goal
-!rewarded(L) : true <- !!rewarded(L).

//	   +blind commitment
+!rewarded(L) : true <- !!rewarded(L).

//	   +relativised commitment
-happiness(X) : ~happiness(Y) & Y>9 <- 
	-rewarded(L);
	.succeed_goal(rewarded(L)).


	
// ANGER SPECIFIC
/* anger causes punishment desire */
+anger(X)[target(L)] : emo_threshold(X) <-
	.remove_plan(helpfullness);
	!punished(L).
	
// begin declarative goal  (p. 174; Bordini,2007)*/
+!punished(L) : punished(L) <- true.

@bPlan[atomic]	
+!punished(L) :has(X) & is_pleasant(eat(X)) <-
	.send(L, achieve, eat(X));
	.print("Asking ", L, " to eat ", X, ". But not shareing necessary ressources. xoxo");
	+punished(L);
	?punished(L).

+punished(L) : true <- 
	!reset_emotion(anger(_), anger(0));
	-punished(L);	
	.succeed_goal(punished(L)).

//     +backtracking goal
-!punished(L) : true <- !!punished(L).

//	   +blind commitment
+!punished(L) : true <- !!punished(L).

//	   + relativised commitment
-anger(X) : ~anger(Y) & Y>9 <- 
	.succeed_goal(punished(L)).