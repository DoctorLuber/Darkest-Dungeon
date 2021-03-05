import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Map {
    private Tile[][] tiles;
    private byte[] currentCoordinates = new byte[2];
    private List<Character> playersTeam = new ArrayList<>();

    public Map(int sizeOfTheMap) {
        this.tiles = new Tile[sizeOfTheMap][sizeOfTheMap];
        this.currentCoordinates = new byte[2];
        currentCoordinates[0] = 1;
        currentCoordinates[1] = 0;
        // make all outter tiles as walls exept the starting positing
        for (int i = 0; i < this.tiles.length; i++) {
            for (int j = 0; j < this.tiles.length; j++) {
                if (i == 1 && j == 0) // starting point
                    this.tiles[i][j] = new Tile(false);
                else if (i == 0)
                    this.tiles[i][j] = new Tile(true); // top walls
                else if (i == sizeOfTheMap - 1)
                    this.tiles[i][j] = new Tile(true); // bottom walls
                else if (j == 0)
                    this.tiles[i][j] = new Tile(true); // left edge
                else if (j == sizeOfTheMap - 1)
                    this.tiles[i][j] = new Tile(true); // right edge
                else
                    this.tiles[i][j] = new Tile(false);
            }
        }
    }

    // Enter "fight" or tell user that the tile is empty
    //
    public void fight() {
        if (!this.getCurrentTile().areEnemiesPresent()) { // if no enemies
            System.out.println("There are no enemies to fight in this tile.");
            return;
        } else
            this.drawFight();

        // if there are enemies in this tile...
        System.out.println("");
        while (this.getCurrentTile().areEnemiesPresent() && !this.isTeamDead()) { // check if enemies or team is dead

            // Player's turn
            for (int i = 0; i < this.getPlayersTeam().size(); i++) {
                Character currentCharacter = this.getPlayersTeam().get(i);
                if (currentCharacter.hasAnyBuffs()) {
                    System.out.println("");
                    currentCharacter.updateBuffs();
                    this.drawFight();
                }

                while (true) { // single character loop
                    System.out.println("==========Current Character [" + (i + 1) + "]==========");

                    int userInput = Game.inputInt(
                            "Enter a number for what you want to do: [1] Basic attac, [2] Special ability, [3] Skip: ",
                            1, 3);
                    if (userInput == 1) {
                        userInput = Game.inputInt("Enter a number for the enemy you want to attac: ", 1,
                                this.getCurrentTile().getEnemies().size()) - 1;
                        currentCharacter.attack(this.getCurrentTile().getEnemies().get(userInput));
                    } else if (userInput == 2) {
                        System.out.println("Name of the ability: " + currentCharacter.getAbilityName());
                        System.out.println("Description: " + currentCharacter.getAbilityDescription());

                        // Apply the buff to an enemy or team
                        List<Character> charactersToApplyAbility = new ArrayList<>();
                        if (currentCharacter.getIsAbilityFriendly() == true) 
                            charactersToApplyAbility = this.getPlayersTeam();
                        else if (currentCharacter.getIsAbilityFriendly() == false)
                            charactersToApplyAbility = this.getCurrentTile().getEnemies();
                        

                        userInput = Game.inputInt("Enter a number for the character you want to use "+ currentCharacter.getAbilityName() + " ability on (0 to cancel): ",0, charactersToApplyAbility.size());
                        if (userInput == 0)
                            continue;
                        else { // apply the special ability
                            currentCharacter.useAbility(charactersToApplyAbility.get(userInput - 1));
                        }

                    } else if (userInput == 3) {
                        System.out.println("Turn skipped.");
                    }

                    removeDead();
                    break; // stop asking the used for this character
                }
                this.drawFight();
            }

            if (this.getCurrentTile().areEnemiesPresent() && !this.isTeamDead()) {
                // Enemies' turn
                this.randomisedAttackForEnemies();
            }

            if (!this.getCurrentTile().areEnemiesPresent()) {
                System.out.println("\n<---ALL ENAMIES DEFETED!--->");
            } else if (this.isTeamDead()) {
                System.out.println("\n<---GAME OVER--->");
                System.exit(0);
            }
        }
    }

    // Draw the "fight"
    //
    public void drawFight() {
        if (!this.getCurrentTile().areEnemiesPresent()) { // if no enemies
            System.out.println("There are no enemies to draw in this tile.");
            return;
        }

        // if there are enemies in this tile...
        System.out.println("\n------------------------------------------------------------");
        this.printCharactersInfo();
        System.out.println("------------------------------------------------------------\n");
    }

    // Print all characters' info
    //
    public void printCharactersInfo() {
        String seperation = "\t\t";
        String smallSeperation = "\t";
        int numOfCharacters = this.getPlayersTeam().size();
        int numOfEnemies = this.getCurrentTile().getEnemies().size();

        for (int i = 0; i < numOfCharacters; i++) { // team character labels
            if (i == 0)
                System.out.print(smallSeperation + "[" + (i + 1) + "]" + smallSeperation);
            else if (i != numOfCharacters - 1)
                System.out.print("[" + (i + 1) + "]" + smallSeperation);
            else
                System.out.print("[" + (i + 1) + "]");
        }

        System.out.print(seperation); // space between

        for (int i = 0; i < numOfEnemies; i++) { // team character labels
            if (i == 0)
                System.out.print("[" + (i + 1) + "]" + smallSeperation);
            else if (i != numOfCharacters - 1)
                System.out.print("[" + (i + 1) + "]" + smallSeperation);
            else
                System.out.print("[" + (i + 1) + "]");
        }
        System.out.println();

        int totalNumOfCharacters = numOfCharacters + numOfEnemies;
        // HEALTH
        System.out.print("Health" + smallSeperation);
        for (int i = 0; i < totalNumOfCharacters; i++) { // print the atributes
            if (i < numOfCharacters) { // it is player's team
                int health = this.getPlayersTeam().get(i).getHealth();

                if (i == 0)
                    System.out.print(health + smallSeperation);
                else if (i != numOfCharacters - 1)
                    System.out.print(health + smallSeperation);
                else
                    System.out.print(health);
            } else { // it is an enemy
                if (i == numOfCharacters)
                    System.out.print(seperation);
                int health = this.getCurrentTile().getEnemies().get(i - numOfCharacters).getHealth();

                if (i != totalNumOfCharacters - 1)
                    System.out.print(health + smallSeperation);
                else
                    System.out.print(health);
            }
        }
        // DAMAGE
        System.out.print("\nDamage" + smallSeperation);
        for (int i = 0; i < totalNumOfCharacters; i++) { // print the atributes
            if (i < numOfCharacters) { // it is player's team
                int damage = this.getPlayersTeam().get(i).getDamage();

                if (i == 0)
                    System.out.print(damage + smallSeperation);
                else if (i != numOfCharacters - 1)
                    System.out.print(damage + smallSeperation);
                else
                    System.out.print(damage);
            } else { // it is an enemy
                if (i == numOfCharacters)
                    System.out.print(seperation);
                int damage = this.getCurrentTile().getEnemies().get(i - numOfCharacters).getDamage();

                if (i != totalNumOfCharacters - 1)
                    System.out.print(damage + smallSeperation);
                else
                    System.out.print(damage);
            }
        }
        // AGILITY
        System.out.print("\nAgility" + smallSeperation);
        for (int i = 0; i < totalNumOfCharacters; i++) { // print the atributes
            if (i < numOfCharacters) { // it is player's team
                int agility = this.getPlayersTeam().get(i).getAgility();

                if (i == 0)
                    System.out.print(agility + smallSeperation);
                else if (i != numOfCharacters - 1)
                    System.out.print(agility + smallSeperation);
                else
                    System.out.print(agility);
            } else { // it is an enemy
                if (i == numOfCharacters)
                    System.out.print(seperation);
                int agility = this.getCurrentTile().getEnemies().get(i - numOfCharacters).getAgility();

                if (i != totalNumOfCharacters - 1)
                    System.out.print(agility + smallSeperation);
                else
                    System.out.print(agility);
            }
        }
        // DEFENCE
        System.out.print("\nDefence" + smallSeperation);
        for (int i = 0; i < totalNumOfCharacters; i++) { // print the atributes
            if (i < numOfCharacters) { // it is player's team
                int defence = this.getPlayersTeam().get(i).getDefence();

                if (i == 0)
                    System.out.print(defence + smallSeperation);
                else if (i != numOfCharacters - 1)
                    System.out.print(defence + smallSeperation);
                else
                    System.out.print(defence);
            } else { // it is an enemy
                if (i == numOfCharacters)
                    System.out.print(seperation);
                int defence = this.getCurrentTile().getEnemies().get(i - numOfCharacters).getDefence();

                if (i != totalNumOfCharacters - 1)
                    System.out.print(defence + smallSeperation);
                else
                    System.out.print(defence);
            }
        }

        System.out.println();
    }

    // Remove any dead characters in team and enemy arrays
    //
    public void removeDead() {
        // Remove dead in team
        for (int i = 0; i < this.getPlayersTeam().size(); i++) {
            Character currentCharacter = this.getPlayersTeam().get(i);

            if (currentCharacter.getHealth() <= 0)
                this.getPlayersTeam().remove(currentCharacter);
        }
        // Remove dead enemies
        for (int i = 0; i < this.getCurrentTile().getEnemies().size(); i++) {
            Character currentCharacter = this.getCurrentTile().getEnemies().get(i);

            if (currentCharacter.getHealth() <= 0)
                this.getCurrentTile().getEnemies().remove(currentCharacter);
        }
    }

    // Randomised attact by the enemy
    public void randomisedAttackForEnemies() {
        List<Character> playersTeam = this.getPlayersTeam();
        List<Character> enemies = this.getCurrentTile().getEnemies();

        for (int i = 0; i < enemies.size(); i++) {
            if (this.isTeamDead())
                return;
            System.out.println("==========Current Enemy ["+(i+1)+"]==========");
            Character currentEnemy = enemies.get(i);
            if (currentEnemy.hasAnyBuffs()) {
                System.out.println("");
                currentEnemy.updateBuffs();
                this.drawFight();
            }
            
            int randomNum = Game.getRandomNumber(1, 100);
            if (randomNum <= 70){ // Attac a team character
                int playerToAttack = Game.getRandomNumber(0, playersTeam.size()-1);
                System.out.println("Attacking Character ["+(playerToAttack+1)+"]");
                currentEnemy.attack(playersTeam.get(playerToAttack));
            }else{ // Use special ability
                if (currentEnemy.getIsAbilityFriendly()){ // if true use on a random enemy
                    int enemyToCastAbilityOn = Game.getRandomNumber(0, enemies.size()-1);
                    System.out.println("Using "+ currentEnemy.getAbilityName() +" on Enemy ["+(enemyToCastAbilityOn+1)+"]");
                    currentEnemy.useAbility(enemies.get(enemyToCastAbilityOn));
                }else{ // use on a team character
                    int charToCastAbilityOn = Game.getRandomNumber(0, playersTeam.size()-1);
                    System.out.println("Using "+ currentEnemy.getAbilityName() +" on Character ["+(charToCastAbilityOn+1)+"]");
                    currentEnemy.useAbility(playersTeam.get(charToCastAbilityOn));
                }
                    
            }

            removeDead();
            this.drawFight();
            
            try {
                TimeUnit.SECONDS.sleep(6);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }

    // Change position of the player on the map
    //
    public void move(byte[] newCoordinates) {
        this.currentCoordinates = newCoordinates;
    }

    public Tile getCurrentTile() {
        return this.tiles[this.currentCoordinates[0]][this.currentCoordinates[1]];
    }

    public byte[] getCurrentCoordinates() {
        return this.currentCoordinates;
    }
    
    public List<Character> getPlayersTeam() {
        return this.playersTeam;
    }

    public void setPlayersTeam(List<Character> newPlayersTeam) {
        this.playersTeam = newPlayersTeam;
    }

    public boolean isTeamDead(){
        if (this.getPlayersTeam().size() <= 0)
            return true;
        else
            return false;
    }

    // Draw a board of any square dimention
    //
    public void draw(){
        int dimention = this.tiles.length;
        String forTrue = "  ";
        String forFalse = "  ";

        // Colour strings
        String reset = "\u001b[0m";
        String redBG = "\u001B[41m";
        String greenBG = "\u001B[42m";

        String isWallColour = greenBG;
        String notWallColour = reset;

        System.out.println("dimention: "+dimention);
        
        // Generate borders
        String topBorder = generateTopBorder(dimention);
        String bottomBorder = generateBottomBorder(dimention);
        String seperatorBorder = generateSeperatorBorder(dimention);

        System.out.println(topBorder);
        for (int i = 0; i <= dimention-1; i++){
            System.out.print(" ┃");
            for (int z = 0; z <= dimention-1; z++){
                
                String lookChange = (this.tiles[i][z].getIsWall()) ? isWallColour : notWallColour;
                
                if (z == dimention-1){
                    // If the boolean is true print 1
                    if (this.tiles[i][z].getIsWall())
                        System.out.print(lookChange + forTrue + reset);
                    else
                        System.out.print(lookChange + forFalse + reset);
                    System.out.print("┃ " + (i+1) + "\n");
                }else{
                    // If the boolean is true print 1
                    if (this.tiles[i][z].getIsWall())
                        System.out.print(lookChange + forTrue + reset);
                    else
                        System.out.print(lookChange + forFalse + reset);
                    System.out.print("┃");
                }
                

            }
            if (i != dimention-1)
                System.out.print(seperatorBorder+"\n");

        }        
        System.out.println(bottomBorder);
    }

    // Generate top border for board of size dimention
    //
    public static String generateTopBorder(int dimention) {
        String topBorder = " ";

        topBorder = topBorder + "┏";
        for (int i = 1; i <= dimention + dimention - 1; i++){
            if (i == (dimention + dimention - 1))
                topBorder = topBorder + "━━";
            else if (i%2 == 0)
                topBorder = topBorder + "┳";
            else
                topBorder = topBorder + "━━";
        }
        topBorder = topBorder + "┓ ";

        return topBorder;
    }


    // Generate seperator border for board of size dimention
    //
    public static String generateSeperatorBorder(int dimention) {
        String topBorder = " ";

        topBorder = topBorder + "┣";
        for (int i = 1; i <= dimention + dimention - 1; i++){
            if (i == (dimention + dimention - 1))
                topBorder = topBorder + "━━";
            else if (i%2 == 0)
                topBorder = topBorder + "╋";
            else
                topBorder = topBorder + "━━";
        }
        topBorder = topBorder + "┫ ";

        return topBorder;
    }


    // Generate bottom border for board of size dimention
    //
    public static String generateBottomBorder(int dimention) {
        String topBorder = " ";

        topBorder = topBorder + "┣";
        for (int i = 1; i <= dimention + dimention - 1; i++){
            if (i == (dimention + dimention - 1))
                topBorder = topBorder + "━━";
            else if (i%2 == 0)
                topBorder = topBorder + "┻";
            else
                topBorder = topBorder + "━━";
        }
        topBorder = topBorder + "┛ ";

        return topBorder;
    }

}