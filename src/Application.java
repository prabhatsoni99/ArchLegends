import java.util.Scanner;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;


/*
HP, XP everywhere are in double, when printing them we do (int)hp


Graph = arr of dimensions 7X7
starting pt = arr[3][3]


herox -> hero
monx -> monster

Flow: Application -> ArchLegends(startHomeScreen) -> ArchLegends(startGameplay)

Graph class ka object has composition (death) relationship with ArchLegends class
Graph class ka object is created inside the ArchLegends class
*/


public class Application
{
	public static void main(String[] args)
	{
		HashMap<String, Hero> hashmap = new HashMap<>();
		//maps from username -> hero_type
		ArchLegends.startHomeScreen(hashmap);
	}
}



class ArchLegends
/*
This is the class where all the management and controller stuff happens
*/
{
	public static void startHomeScreen(HashMap<String, Hero> hashmap) //takes map as parameter
	{
		Scanner input = new Scanner(System.in);

		System.out.println("Welcome to ArchLegends");
		System.out.println("Choose your option");
		System.out.println("1) New User");
		System.out.println("2) Existing User");
		System.out.println("3) Exit");

		int choice = input.nextInt();
		if(choice==1)
		{
			System.out.println("Enter Username");
			String name = input.next();

			System.out.println("Choose a Hero");
			System.out.println("1) Warrior");
			System.out.println("2) Thief");
			System.out.println("3) Mage");
			System.out.println("4) Healer");
			int choiceHero = input.nextInt();
			if(choiceHero==1)
			{
				Hero HeroObj = new Warrior();
				hashmap.put(name, HeroObj);
			}
			else if(choiceHero==2)
			{
				Hero HeroObj = new Thief();
				hashmap.put(name, HeroObj);
			}
			else if(choiceHero==3)
			{
				Hero HeroObj = new Mage();
				hashmap.put(name, HeroObj);
			}
			else if(choiceHero==4)
			{
				Hero HeroObj = new Healer();
				hashmap.put(name, HeroObj);
			}
			startHomeScreen(hashmap);
		}


		else if(choice==2)
		{
			System.out.println("Enter username");
			String username = input.next();
			if(hashmap.containsKey(username))
			{
				Hero HeroObj = hashmap.get(username);
				startGameplay(HeroObj, hashmap);
			}
			else
			{
				System.out.println("Username: " + username + " was not found");
				startHomeScreen(hashmap);
			}
		}


		else if(choice==3)
		{
			System.exit(0);
		}
	}



	/*************************	reincarnator	******************************/
	public static void reincarnator(Hero herox) //polymorphism
	{
		herox.reincarnateHero();
	}

	public static void reincarnator(Monster monx) //polymorphism
	{
		monx.reincarnateMonster();
	}





	/*****************************	startGameplay()	*********************************/




	public static void startGameplay(Hero herox, HashMap<String, Hero> hashmap)
	{

		//We make graph here since this func never is never called recursively
		Graph graph = new Graph();
		Scanner input = new Scanner(System.in);


		while(true) //can do better than this I guess lol
		{

			int x = herox.getx_coordinate();
			int y = herox.gety_coordinate();

			graph.printOptions(x,y);
			System.out.println("Enter x & y coordinate you want to visit (seperated by space): ");

			int[] arr = graph.getBestNextPath(x,y,herox.getLevel());
			System.out.println("Our recommendation system suggests " + arr[0] + "," + arr[1]);

			int i = input.nextInt();
			int j = input.nextInt();


			herox.changeCoordinates(i,j); //changes coordinates of hero
			Monster monx = graph.getMonsterAtCoordinate(i,j); //alternatively get current coordinates of hero


			String whoWon = simulateFightHeroMonster(herox, monx);
			if(whoWon=="monster")
			{
				System.out.println("Monster won, so we are going back to home screen");
				reincarnator(herox);
				startHomeScreen(hashmap);
				return;
			}
			else if(whoWon=="hero" && monx.getLevel()==4)
			{
				System.out.println("Congrats, you just defeated the great LionFang");
				System.out.println("GAME WON");
				System.out.println("Returning to home screen");
				reincarnator(herox);
				startHomeScreen(hashmap);
				return;
			}

			else if(whoWon=="hero")
				//the main case - where the game goes on / continues
			{
				System.out.println("Congrats! You defeated a monster!");
				//below 3 actions must happen in this order only
				herox.increaseXP(monx.getLevel()*20); //increase XP by this much

				if(herox.get_currentlyUsingSidekick()==true)
				{
					herox.getBestSidekick().increaseXPBy(monx.getLevel()*2); // 20/10 = 2
					herox.getBestSidekick().updateAttackPowerFromXP();
					herox.getBestSidekick().resetHP();
				}

				//asking if hero wants to buy a Sidekick
				System.out.println("Do you want to buy a sidekick? (Y/N)");
				String want_sidekick = input.next();
				if(want_sidekick.equals("Y"))
				{
					herox.buySidekick();
				}


				herox.ChangeHeroLevelByXP();
				if(herox.getLevel()==4) //& lastlevel!=4?
					graph.UpdateGraphForLevel4();
				herox.refreshHPForNextFight();
				//this will continue now in while loop!
				//donot make recursive call here otherwise graph will get refreshed
				//re-incarnating monster below
				reincarnator(monx);

				herox.set_currentlyUsingSidekick(false); // everytime we assume hero will not use sidekick!

			}
		}


	}





	/*****************************	getRandomMonster()	*********************************/



	public static Monster getRandomMonster()
	/*
	Gives a random monster of level 1-3
	*/
	{
		Random rand = new Random();
		int monsterLevel = rand.nextInt(3) + 1; //gives from 1-3

		if(monsterLevel==1)
		{
			//can we do returning part in 1 line?
			Monster thisMonster = new Goblin();
			return thisMonster;
		}
		else if(monsterLevel==2)
		{
			Monster thisMonster = new Zombie();
			return thisMonster;
		}
		else if(monsterLevel==3)
		{
			Monster thisMonster = new Fiend();
			return thisMonster;
		}

		return new Goblin(); //just coz Java wants me to do it

	}



	/*****************************	simulateFightHeroMonster()	*********************************/




	public static String simulateFightHeroMonster(Hero herox, Monster monx)
	//the string we are returning is hero or monster to tell who won
	{
		int spTurnsLeft = 0;
		String spChoice = "just for intiialising";
		Scanner input = new Scanner(System.in);
		int heroMoves = 1;
		System.out.println("You are fighting a monster of level " + monx.getLevel());

		if(herox.mysidekicks.size() > 0)
		{
			System.out.println("Do you want to use your sidekick? (Y/N)");
			String ans = input.next();
			if(ans.equals("Y"))
			{

				Sidekick bestsidekick = herox.getBestSidekick();
				herox.setCurrentSidekick(bestsidekick);
				herox.set_currentlyUsingSidekick(true);
				if(bestsidekick instanceof Minion)
				{
					System.out.println("Do you want to create clones? (Y/N)");
					String ch = input.next();
					if(ch.equals("Y"))
						bestsidekick.createClones();
				}

				if(bestsidekick instanceof Knight && monx.getLevel()==2)
					bestsidekick.increaseDefenceAbilityOfHero(monx, herox);
			}

		}

		while(herox.getHP()>0 && monx.getHP()>0)
		//even if this is whil(true) fucn will work
		// it is very unlikely the above while() will not be satisfied
		{

			if(spTurnsLeft > 0)
			//its okay if spTurnsLeft has some random value coz 1st condition is this!
			{
				System.out.println("Special power executed!");
				System.out.println("Will be done for next " + spTurnsLeft + " moves of hero");

				if(spChoice=="warrior-sp")
				{
					if(spTurnsLeft==1)
					{
						herox.changeAttackPower(-5); //to reduce
						herox.changeDefensePower(-5); //to reduce
					}
					else if(spTurnsLeft==3)
					{
						herox.changeAttackPower(5); //to increase
						herox.changeDefensePower(5); //to increase
					}
				}
				else if(spChoice=="mage-sp")
				{
					monx.reduceHPBy( monx.getHP() * 0.05 );
				}
				else if(spChoice=="healer-sp")
				{
					herox.increaseXP(herox.getHP() * 0.05);
				}

				spTurnsLeft--;
			}




			System.out.println("Choose move");
			System.out.println("1) Attack");
			System.out.println("2) Defence");
			if(heroMoves%4==0)
				System.out.println("3) Special move");

			int heroChoice = input.nextInt();
			if(heroChoice==1)
			{
				herox.doAttack(monx);
				System.out.println("You attacked!");
			}
			else if(heroChoice==2)
			{
				herox.doDefense();
				System.out.println("You defensed!");
			}
			else if(heroMoves%4==0 && heroChoice==3)
			{
				System.out.println("You used your special move!");
				spChoice = herox.doSpecialPower(monx);
				spTurnsLeft = 3;
			}

			heroMoves++;

			System.out.println( "Your HP: " + (int)herox.getHP() + "\t\tMonster HP: " + (int)monx.getHP() );

			if(monx.getHP()<=0)
				return "hero";


			//Now Monster's turn
			monx.attackHero(herox);
			System.out.println("Monster attacked!");
			System.out.println( "Your HP: " + (int)herox.getHP() + "\t\tMonster HP: " + (int)monx.getHP() );


			if(herox.getHP()<=0)
				return "monster";
		}

		return "string only returned so we dont get runtime error";
	}




}


/*************************************************************************
*********************	Graph class BELOW		**************************
**************************************************************************/


class Graph
{
	/*
	Assumption: We don't go out of bounds of graph, the graph is big enough
	Does not contain cur_ptr, that is in Hero class
	*/
	Monster[][] matrix;

	Graph()
	{
		matrix = new Monster[7][7];
		for(int i=0;i<7;i++)
			for(int j=0;j<7;j++)
				matrix[i][j] = ArchLegends.getRandomMonster();
	}


	public void printOptions(int i, int j)
	{
		System.out.println("You are currently at " + i + "," + j);
		System.out.println("Your options are: ");
		System.out.println((int)(i-1) + "," + (int)(j-1));
		System.out.println((int)(i+1) + "," + (int)(j-1));
		System.out.println((int)(i+1) + "," + (int)(j+1));
		System.out.println((int)(i-1) + "," + (int)(j+1));
	}


	public int[] getBestNextPath(int i, int j, int levelHero)
	{
		//ideal is monster_level = hero_level
		int[] arr = new int[2]; //x,y coordinate
		int a, b;

		a=i-1; b=j-1;
		if(levelHero == getMonsterAtCoordinate(a,b).getLevel())
		{
			arr[0]=a;arr[1]=b;
			return arr;			
		}

		a=i+1; b=j-1;
		if(levelHero == getMonsterAtCoordinate(a,b).getLevel())
		{
			arr[0]=a;arr[1]=b;
			return arr;	
		}

		a=i-1; b=j+1;
		if(levelHero == getMonsterAtCoordinate(a,b).getLevel())
		{
			arr[0]=a;arr[1]=b;
			return arr;	
		}

		a=i+1; b=j+1;
		if(levelHero == getMonsterAtCoordinate(a,b).getLevel())
		{
			arr[0]=a;arr[1]=b;
			return arr;	
		}

		//no immediate neighbour is of same level
		//now we will check where diff=1, there definitely would exist such a case
		a=i-1; b=j-1;
		if( Math.abs(levelHero - getMonsterAtCoordinate(a,b).getLevel()) <=1 )
		{
			arr[0]=a;arr[1]=b;
			return arr;			
		}

		a=i+1; b=j-1;
		if( Math.abs(levelHero - getMonsterAtCoordinate(a,b).getLevel()) <=1 )
		{
			arr[0]=a;arr[1]=b;
			return arr;	
		}

		a=i-1; b=j+1;
		if( Math.abs(levelHero - getMonsterAtCoordinate(a,b).getLevel()) <=1 )
		{
			arr[0]=a;arr[1]=b;
			return arr;	
		}

		a=i+1; b=j+1;
		if( Math.abs(levelHero - getMonsterAtCoordinate(a,b).getLevel()) <=1 )
		{
			arr[0]=a;arr[1]=b;
			return arr;	
		}


		//if nothing works
		arr[0] = i+1;
		arr[1] = j;
		return arr;

	}



	public Monster getMonsterAtCoordinate(int i, int j)
	{
		return this.matrix[i][j];
	}


	public void UpdateGraphForLevel4()
	//dont change those ones which are already visited - have to implement that
	{
		Random rand = new Random();
		for(int i=0;i<7;i++)
		{
			for(int j=0;j<7;j++)
			{
				//0 for mon lev 1-3
				//1 for mon lev 4
				int decider = rand.nextInt(2); //gives 0-1
				if(decider==0)
					matrix[i][j] = ArchLegends.getRandomMonster();
				else if(decider==1)
					matrix[i][j] = new Lionfang();
			}
		}
		System.out.println("Map now contains the great Lionfang!!");
	}


}







/*************************************************************************
*********************	MONSTER STUFF BELOW		**************************
**************************************************************************/




class Monster
{
	protected int level;
	protected double HP;
	protected Random rand = new Random();

	public int getLevel() { return this.level; }

	public double getHP() { return this.HP; }

	public void attackHero(Hero herox)
	{
		double gaussianRV = rand.nextGaussian()*0.06 + 0.08; //sets mean = 0.08, s.d. = 0.06
		gaussianRV = Math.max(0, gaussianRV);
		gaussianRV = Math.min(0.25, gaussianRV);
		//the above 2 lines is a very intelligent implementation to ensure 0<=gaussianRV<=0.25

		double howMuchToReduceHPBy = this.HP * gaussianRV;
		herox.reduceHPBy(howMuchToReduceHPBy);
		System.out.println("Monster inflicted damage of " + (int)howMuchToReduceHPBy + " HP on you");
	}

	public void reincarnateMonster()
	{
		this.HP = (this.level+1)*50;
	}


	public void reduceHPBy(double decreaseByMe)
	{
		this.HP -= decreaseByMe;
		this.HP = Math.max(0, this.HP);
	}

}



class Goblin extends Monster
{
	Goblin()
	{
		this.level = 1;
		this.HP = 100;
	}
}

class Zombie extends Monster
{
	Zombie()
	{
		this.level = 2;
		this.HP = 150;
	}
}

class Fiend extends Monster
{
	Fiend()
	{
		this.level = 3;
		this.HP = 200;
	}
}

class Lionfang extends Monster
{
	Lionfang()
	{
		this.level = 4;
		this.HP = 250;
	}

	@Override
	public void attackHero(Hero herox) //should this be private or protected?
	{
		int output = rand.nextInt(10); //generates form 0-9
		if(output==1)
		{
			double howMuchToReduceHPBy = herox.getHP() * 0.5;
			herox.reduceHPBy(howMuchToReduceHPBy);
			System.out.println("Lionfang used its special move!");
			System.out.println("Lionfang inflicted " + (int)howMuchToReduceHPBy + " damage on you");
		}
		else
		{
			super.attackHero(herox); //calling the parent class ka attackHero()
		}		

	}

}


/*************************************************************************
*********************	HERO STUFF BELOW	******************************
**************************************************************************/





abstract class Hero //abstract class
{
	protected double HP = 100;
	protected int XP = 0;
	protected int level = 1;
	protected boolean defenceOn = false;

	protected int attackAttribute;
	protected int defenseAttribute;

	public int x_coordinate = 3;
	public int y_coordinate = 3;


	public int getx_coordinate() { return this.x_coordinate; }
	public int gety_coordinate() { return this.y_coordinate; }


	public void reincarnateHero()
	{
		this.HP = 100;
		this.XP = 0;
		this.level = 1;
		this.defenceOn = false;
		this.x_coordinate = 3;
		this.y_coordinate = 3;
	}

	public void changeCoordinates(int i, int j)
	{
		this.x_coordinate = i;
		this.y_coordinate = j;
	}

	public int getLevel() { return this.level; }

	public double getHP() { return this.HP; }

	protected void doAttack(Monster monx)
	{
		double do_damage_of;

		if(this.get_currentlyUsingSidekick()==true)
		{
			do_damage_of = this.attackAttribute + this.currentSidekick.giveDamage();
			System.out.println("Sidekick did damage of " + this.currentSidekick.giveDamage() + " HP on monster");
		}

		else
			do_damage_of = (double)this.attackAttribute;

		monx.reduceHPBy(do_damage_of);
		System.out.println("You did damage of " + this.attackAttribute + " HP on monster");
	}
	
	public void increaseXP(double incrementByMe) { this.XP += incrementByMe; }

	public void ChangeHeroLevelByXP()
	{
		this.level = Math.min(4 , this.XP/20 + 1);
		System.out.println("You are currently level " + this.level);
	}	

	public void refreshHPForNextFight()
	{
		this.HP = (this.level+1)*50;
	}

	public void reduceHPBy(double decreaseByMe)
	{
		if(this.defenceOn)
		{
			double possibleNewHP = this.HP - decreaseByMe + this.defenseAttribute;
			this.HP = Math.min(this.HP, possibleNewHP); //to ensure when defending HP doesnt increase lol
			this.HP = Math.max(0, this.HP);
			this.defenceOn = false;
		}
		else
		{
			this.HP = this.HP - decreaseByMe;
			this.HP = Math.max(0, this.HP);
		}

		// for sidekick, defenceAttribute doesnt matter
		// so we will just reduce by 1.5 * decreasebyMe
		if(this.get_currentlyUsingSidekick()==true)
		{
			this.getCurrentSidekick().takeDamage(1.5*decreaseByMe);

			System.out.println("Sidekick took damage of " + (int)1.5*decreaseByMe + " Sidekick HP: " + (int)this.getCurrentSidekick().getHP());

			if(this.getCurrentSidekick().getHP() <= 0)
				this.set_currentlyUsingSidekick(false); //solves everything!
		}

	}


	protected void changeAttackPower(double val) {this.attackAttribute += val; }
	protected void changeDefensePower(double val) {this.defenseAttribute += val; }


	protected void doDefense()
	{
		this.defenceOn = true;
	}


	abstract public String doSpecialPower(Monster monx); //abstract method







	////////////////////////		Sidekick related stuff below

	// Here, Hero class has association relationship with Sidekick class
	// Since it has a field of type Sidekick

	ArrayList<Sidekick> mysidekicks = new ArrayList<Sidekick>();

	//currentSidekick is never accessed unless currentlyUsingSidekick is true
	private boolean currentlyUsingSidekick = false;
	private Sidekick currentSidekick;

	public void set_currentlyUsingSidekick(boolean users_wish) {this.currentlyUsingSidekick = users_wish;}
	public boolean get_currentlyUsingSidekick() {return this.currentlyUsingSidekick;}

	public void setCurrentSidekick(Sidekick sk) {this.currentSidekick = sk;}
	public Sidekick getCurrentSidekick() {return this.currentSidekick;}

	public Sidekick getBestSidekick()
	{
		//returns best sidekick based on XP
		//need to implement this

		if(mysidekicks.size() <= 0)
		{
			System.out.println("Error: there exist no sidekicks");
			Sidekick sk = new Minion(5);
			return sk; //to escape error
		}


		Sidekick curbest = new Minion(5); //wont give error


		for(int i=0;i<mysidekicks.size();i++)
		{
			//System.out.println("Inside loop, iteration " + i);
			curbest = mysidekicks.get(i).compareTo(curbest);
		}
		return curbest;
	}

	public void removeSidekick(Sidekick sk)
	{
		for(int i=0;i<mysidekicks.size();i++)
			if(mysidekicks.get(i)==sk)
			{
				mysidekicks.remove(i);
				break; //not reqd maybe
			}
	}


	public void buySidekick()
	{
		Scanner input = new Scanner(System.in);
		System.out.println("1. Buy Minion");
		System.out.println("2. Buy Knight");
		int choice = input.nextInt();

		System.out.println("You currently have " + this.XP + " XP");
		System.out.println("How much XP of yours do you want to spend?");
		if(choice==1)
			System.out.println("Base cost = " + 5);
		else if(choice==2)
			System.out.println("Base cost = " + 8);
		double spentXP = input.nextDouble();
		this.XP -= spentXP;

		if(choice==1)
		{
			Sidekick sk = new Minion(spentXP);
			mysidekicks.add(sk);
		}
		else if(choice==2)
		{
			Sidekick sk = new Knight(spentXP);
			mysidekicks.add(sk);
		}
		else
		{
			System.out.println("Valid entry is 1-2");
			return;
		}


		System.out.println("Sidekick added successfully");
	}


}


class Warrior extends Hero
{
	Warrior()
	{
		//cannot keep these both as this.ibecause
		//they can change accoridng to a special power
		this.attackAttribute = 10;
		this.defenseAttribute = 3;
	}
	//Special power: Attack and defense attributes get boosted by 5 for the next 3 moves.

	@Override
	public String doSpecialPower(Monster monx)
	{
		return "warrior-sp";
	}

}

class Mage extends Hero
{
	Mage()
	{
		this.attackAttribute = 5;
		this.defenseAttribute = 5;
	}
	//Special power: Cast a spell which reduces the opponent's HP by 5% for the next 3 moves.
	@Override
	public String doSpecialPower(Monster monx)
	{
		return "mage-sp";
	}
}

class Thief extends Hero
{
	Thief()
	{
		this.attackAttribute = 6;
		this.defenseAttribute = 4;
	}
	//Special power: Steal 30% of opponents HP.
	@Override
	public String doSpecialPower(Monster monx)
	{
		this.HP += 0.3*monx.getHP();
		double reduceBy = 0.3*monx.getHP();
		monx.reduceHPBy(reduceBy);
		return "thief-sp"; //never used in the func that calls it
	}
}

class Healer extends Hero
{
	Healer()
	{
		this.attackAttribute = 4;
		this.defenseAttribute = 8;
	}
	//Special power: Increase own HP by 5% for the next 3 moves.
	@Override
	public String doSpecialPower(Monster monx)
	{
		return "healer-sp";
	}
}


/*********************************************************************************************
*****************************	Sidekick stuff below 	**************************************
**********************************************************************************************/


abstract class Sidekick implements Comparable<Sidekick>
{
	protected double HP = 100;
	protected double XP = 0;
	protected double attackPower; //initialised in subclass constructors

	public double getHP() {return this.HP;}
	public double getXP() {return this.XP;}
	public void increaseXPBy(double inc) {this.XP += inc;}
	public void resetHP() {this.HP=100;}


	@Override
	public Sidekick compareTo(Sidekick s2) //comparing objects
	{
		if(this.XP >= s2.XP)
			return this;
		else
			return s2;
	}


	public void updateAttackPowerFromXP()
	{
		this.attackPower = 0.2 * this.XP; //5XP -> 1 attack power (for sidekick)
	}

	public void takeDamage(double reduceBy)
	{
		this.HP -= reduceBy;
		this.HP = Math.max(0, this.HP);
	}

	public double giveDamage()
	{
		return this.attackPower;
	}

	abstract public void createClones();
	abstract public void increaseDefenceAbilityOfHero(Monster monx, Hero herox);


}




class Minion extends Sidekick implements Cloneable
{
	Minion(double givenXP)
	{
		boolean clone_used = false;

		this.attackPower = 1 + (givenXP - 5)*0.5; //assumes givenXP >= 5
		if(givenXP>0)
			System.out.println("created a minion of " + this.attackPower);
		if(givenXP<5)
			System.out.println("Error: You gave " + givenXP + " but you need to give atleast 5XP for minion");
	}
	boolean clone_used = false;


	public Sidekick clone()
	{
		Sidekick m2 = (Minion)this;
		return m2;
	}


	public void increaseDefenceAbilityOfHero(Monster monx, Hero herox)
	{
		//do nothing
		return;
	}

	@Override
	public void createClones()
	{
		Sidekick m1 = this.clone();
		Sidekick m2 = this.clone();
		Sidekick m3 = this.clone();
		this.attackPower += m1.attackPower + m2.attackPower + m3.attackPower;
		this.clone_used = false;
	}

}


class Knight extends Sidekick
{
	Knight(double givenXP)
	{
		this.attackPower =  2 + (givenXP - 8)*0.5; //assumes givenXP >= 5
		if(givenXP>0)
			System.out.println("created a knight of " + this.attackPower);
		if(givenXP<5)
			System.out.println("Error: You gave " + givenXP + " but you need to give atleast 8XP for knight");
	}

	@Override
	public void increaseDefenceAbilityOfHero(Monster monx, Hero herox)
	{
		//will be called at start of fight, only once
		if(monx.getLevel()==2)
			herox.changeDefensePower(5);
	}

	@Override
	public void createClones()
	{
		//do nothing
		return;
	}

}


