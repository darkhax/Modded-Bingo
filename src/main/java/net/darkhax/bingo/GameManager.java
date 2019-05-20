package net.darkhax.bingo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.darkhax.bingo.api.Goal;
import net.darkhax.bingo.api.GoalTable;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class GameManager {

    private GoalTable table;
	private Random random;
    private Goal[][] goals = new Goal[5][5];
    private Team[][][] completionStates = new Team[5][5][4];
    private boolean isActive = false;
    private boolean hasStarted = false;    
    
    public void create(Random random, GoalTable table) {
    	
    	this.reset();
    	this.table = table;
    	this.random = random;
    	this.rollGoals(random);
    	this.isActive = true;
    }
    
    public void start() {
    	
    	this.hasStarted = true;
    }
    
    public void end() {
    	
    	this.hasStarted = false;
    	this.isActive = false;
    	this.reset();
    }
    
    public void reset() {
    	
    	goals = new Goal[5][5];
    	completionStates = new Team[5][5][4];
    	hasStarted = false;
    	table = null;
    }
    
    public Goal getGoal(int x, int y) {
    	
    	return goals[x][y];
    }
    
    public void setGoal(int x, int y, Goal goal) {
    	
    	goals[x][y] = goal;
    }
    
    public void setGoalComplete(EntityPlayerMP player, int x, int y) {
    	
    	final Team playerTeam = BingoPersistantData.getTeam(player);
    	
    	completionStates[x][y][playerTeam.getTeamCorner()] = playerTeam;
    	
    	if (player != null) {
    		
    		final Goal goal = this.getGoal(x, y);
    		
    		ITextComponent playerName = player.getDisplayName();
    		playerName.getStyle().setColor(playerTeam.getTeamColorText());
    		
    		ITextComponent itemName = goal.getTarget().getTextComponent();
    		itemName.getStyle().setColor(TextFormatting.GRAY);
    		
    		player.server.getPlayerList().sendMessage(new TextComponentTranslation("bingo.player.obtained", playerName, itemName));

    		
    		EntityFireworkRocket rocket = new EntityFireworkRocket(player.getEntityWorld(), player.posX, player.posY, player.posZ, playerTeam.getFireworStack());
    		ObfuscationReflectionHelper.setPrivateValue(EntityFireworkRocket.class, rocket, 0, "field_92055_b");
    		player.getEntityWorld().spawnEntity(rocket);
    		
    		System.out.println(playerTeam.getFireworStack().getTagCompound().toString());
    	}
    }
    
    public boolean hasCompletedGoal(int x, int y, Team team) {
    	
    	return completionStates[x][y][team.getTeamCorner()] != null;
    }
    
    public Team[] getCompletionStats(int x, int y) {
    	
    	return completionStates[x][y];
    }
    
    public Team checkWinState() {
    	
    	for (Team team : BingoMod.TEAMS) {
    		
    		// Check vertical lines
        	for (int x = 0; x < 5; x++) {
        		
        		boolean hasFailed = false;
        		
        		for (int y = 0; y < 5; y++) {
        			
        			if (!hasCompletedGoal(x, y, team)) {
        				
        				hasFailed = true;
        				break;
        			}
        		}
        		
        		if (!hasFailed) {
        			
        			return team;
        		}
        	}
        	
    		// Check horizontal lines
        	for (int y = 0; y < 5; y++) {
        		
        		boolean hasFailed = false;
        		
        		for (int x = 0; x < 5; x++) {
        			
        			if (!hasCompletedGoal(x, y, team)) {
        				
        				hasFailed = true;
        				break;
        			}
        		}
        		
        		if (!hasFailed) {
        			
        			return team;
        		}
        	}
        	
        	// Check one horizontal line
        	if (this.hasCompletedGoal(0, 0, team) && this.hasCompletedGoal(1, 1, team) && this.hasCompletedGoal(2, 2, team) && this.hasCompletedGoal(3, 3, team) && this.hasCompletedGoal(4, 4, team)) {
        		
        		return team;
        	}
        	
        	// Check other horizontal line
        	if (this.hasCompletedGoal(0, 4, team) && this.hasCompletedGoal(1, 3, team) && this.hasCompletedGoal(2, 2, team) && this.hasCompletedGoal(3, 1, team) && this.hasCompletedGoal(4, 0, team)) {
        		
        		return team;
        	}
    	}
    	
    	return null;
    }
    
    public void onPlayerPickupItem(EntityPlayerMP player, ItemStack stack) {
    	
    	if (BingoMod.GAME_STATE.isHasStarted()) {
    		
        	for (int x = 0; x < 5; x++) {
        		
        		for (int y = 0; y < 5; y++) {
        			
        			final Goal goal = getGoal(x, y);
        			
        			if (ItemStack.areItemStacksEqual(goal.getTarget(), stack)) {
        				
        				setGoalComplete(player, x, y);
        			}
        		}
        	}
    	}
    }
    
    public void rollGoals(Random rand) {
    	  	
    	List<Goal> generatedGoals = new ArrayList<>();
    	
    	for (int i = 0; i < 25; i++) {
    		
    		Goal goal = null;
    		
    		while (goal == null) {
    			
    			
        		final Goal randGoal = table.getRandomTier(rand).getRandomGoal(rand);
        		
        		if (!generatedGoals.contains(randGoal)) {
        			
        			goal = randGoal;
        		}
        		
    		}
    		
    		generatedGoals.add(goal);
    	}
    	
    	Collections.shuffle(generatedGoals);
    	
    	int xOff = 0;
    	int yOff = 0;
    	
    	for (Goal goal : generatedGoals) {
    		
    		this.setGoal(xOff, yOff, goal);
    		
    		xOff++;
    		if (xOff == 5) {
    			
    			xOff = 0;
    			yOff++;
    		}
    	}
    }

	public GoalTable getTable() {
		return table;
	}

	public void setTable(GoalTable table) {
		this.table = table;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public Goal[][] getGoals() {
		return goals;
	}

	public void setGoals(Goal[][] goals) {
		this.goals = goals;
	}

	public Team[][][] getCompletionStates() {
		return completionStates;
	}

	public void setCompletionStates(Team[][][] completionStates) {
		this.completionStates = completionStates;
	}

	public boolean isHasStarted() {
		return hasStarted;
	}

	public void setHasStarted(boolean hasStarted) {
		this.hasStarted = hasStarted;
	}

	public boolean isActive() {
		return isActive;
	}
}