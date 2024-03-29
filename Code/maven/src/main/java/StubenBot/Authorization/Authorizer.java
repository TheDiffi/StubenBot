package StubenBot.Authorization;

import java.util.ArrayList;
import java.util.Arrays;

import StubenBot.CommandProperties;
import StubenBot.Globals;
import StubenBot.Main;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;

public class Authorizer {

    // reads the file and returns the list of IDs
    public static ArrayList<AuthID> readAuthorizedIDs(String filename) {
        try {
            String rawContent = Globals.readTxt(filename);
            return parseIDs(rawContent);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /*
     * parses the raw String into IDs. e.g.: rawContent =
     * testUser:1;testUsersdfbsdfbs:2;sfgbdfhbfghb:0; rawIDs = [testUser:1,
     * testUsersdfbsdfbs:2, sfgbdfhbfghb:0] parsedIDs =
     * [StubenBot.AuthorizedID@343f4d3d, StubenBot.AuthorizedID@53b32d7,
     * StubenBot.AuthorizedID@5442a311]
     */
    private static ArrayList<AuthID> parseIDs(String rawContent) {
        var rawIDs = Arrays.asList(rawContent.split(";"));
        ArrayList<AuthID> parsedIDs = new ArrayList<>();
        var duplicateBuffer = new ArrayList<String>();

        // parses into ID objects
        for (String str : rawIDs) {
            var parameters = Arrays.asList(str.split(":"));
            var ID = new AuthID(parameters.get(0), Integer.parseInt(parameters.get(1)));

            if (!checkDuplicates(ID, duplicateBuffer)) {
                parsedIDs.add(ID);
            }
            duplicateBuffer.add(parameters.get(0));
        }
        return parsedIDs;
    }

    /**
     * @return True if duplicate id found
     */
    private static boolean checkDuplicates(AuthID ID, ArrayList<String> duplicateBuffer) {
        for (String prevID : duplicateBuffer) {
            if (ID.id.equals(prevID)) {
                System.out.println("WARNING: duplicate ID found: " + ID.id + "; Skipping this object");
                return true;
            }
        }
        return false;
    }

    /**
     * gets the authors authLVL
     * 
     * @param event
     * @param authList list with registered ids (Main.authorizations)
     * @return the authLVL as an INT
     */
    public static int getAuthorizationLevel(MessageCreateEvent event, ArrayList<AuthID> authList) {
        var channelID = event.getMessage().getChannelId().asString();
        var authorID = event.getMessage().getAuthor().get().getId().asString();

        // looks for a matching id
        for (var ID : authList) {
            if (authorID.equals(ID.id) || channelID.equals(ID.id)) {
                return ID.level;
            }
        }

        // if none found -> standard = 0
        return 0;
    }

    /**
     * gets the authLVL from a authList
     * 
     * @param unknownID gets lvl for this id
     * @param authList  list with registered ids (Main.authorizations)
     * @return the authLVL as an INT
     */
    public static int getAuthorizationLevel(String unknownID, ArrayList<AuthID> authList) {

        // looks for a matching id
        for (var ID : authList) {
            if (unknownID.equals(ID.id)) {
                return ID.level;
            }
        }

        // if none found -> standard = 0
        return 0;
    }

    /**
     * changes or adds an authLVL of a authID in a authList
     * 
     * @return if it worked as a bool
     */
    public static boolean changeAuthorization(String cID, int newLVL, ArrayList<AuthID> authList, String filename) {
        // needs to check if the id already has a lvl; if not: addAuth(); if yes then it
        // needs to read, change and write the whole txt
        if (getAuthID(cID, authList) == null) {
            // if the id is not present in the list, it gets simply appended
            var newAuth = new AuthID(cID, newLVL);
            Globals.appendToTxt(filename, newAuth.toString() + ";");

        } else {
            // gets & edits the raw content, then puts it back in
            try {
                var tempList = readAuthorizedIDs(filename);
                tempList.remove(getAuthID(cID, tempList));
                var newID = new AuthID(cID, newLVL);
                tempList.add(newID);

                String newAuthContent = "";
                for (AuthID authID : tempList) {
                    newAuthContent += authID.toString();
                }
                Globals.writeToTxt(filename, newAuthContent);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        // TODO: this should be authList = readAuthorizedIDs(filename); but somehow it
        // changes the instance of the variable and so it doesnt change
        // Man.authorizations ig that variable gets fed into the funktion
        Main.authorizations = readAuthorizedIDs(filename);

        return true;
    }

    public static boolean deleteAuthorization(String cID, ArrayList<AuthID> authList, String filename) {
        // if an auth is present, remove it
        if (getAuthID(cID, authList) != null) {
            // gets & edits the raw content, then puts it back in
            try {
                var tempList = readAuthorizedIDs(filename);
                tempList.remove(getAuthID(cID, tempList));

                String newAuthContent = "";
                for (AuthID authID : tempList) {
                    newAuthContent += authID.toString();
                }
                Globals.writeToTxt(filename, newAuthContent);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        // "reloads" the list
        authList = readAuthorizedIDs(filename);
        return true;
    }

    public static boolean deleteAuthorization(AuthID ID, ArrayList<AuthID> authList, String filename) {
        return deleteAuthorization(ID.id, authList, filename);
    }

    /**
     * Checks if the authID exists within the authList
     * 
     * @return The AuthID-Object if found, else returns null
     */
    private static AuthID getAuthID(String id, ArrayList<AuthID> authList) {
        for (AuthID authID : authList) {
            if (authID.id.equals(id)) {
                return authID;
            }
        }

        return null;
    }

    public static String getUserByID(MessageCreateEvent event) {
        var otherID = "";
        try {
            otherID = event.getMessage().getContent().split(" ")[1];
        } catch (IndexOutOfBoundsException e) {
            otherID = null;
        }
        if (otherID != null) {
            // looks for a matching id
            var usr = Main.client.getUserById(Snowflake.of(otherID));
            return usr.block().getUsername();
        } else {
            return event.getMessage().getAuthor().get().getUsername();
        }

    }

    public static void changeAuthLVL(MessageCreateEvent event, CommandProperties props) {
        if (props.params.size() == 2) {
            var id = props.params.get(0);
            try {
                var lvl = Integer.parseInt(props.params.get(1));
                if (lvl >= 0 && lvl <= 10) {
                    if (Authorizer.getAuthorizationLevel(event, Main.authorizations) > lvl) {
                        if (Authorizer.changeAuthorization(id, lvl, Main.authorizations, Main.authFilepath)) {
                            Globals.createEmbed(props.eventChannel, Color.MAGENTA, "",
                                    "Changed the Authorization Level of " + id + " to " + lvl + ".");
                        }
                    } else {
                        Globals.createEmbed(props.eventChannel, Color.RED, "",
                                "Your Authorization Level is not high enough for this Command.");
                    }
                } else {
                    Globals.createEmbed(props.eventChannel, Color.RED, "", "Choose a Level between 0 and 10 ");
                }
            } catch (Exception e) {
                System.out.println("CommandDistributer: changeAuthLVL: Could not Convert to int");
                Globals.createEmbed(props.eventChannel, Color.RED, "",
                        "Unsuccessful: second parameter must be an Integer");
            }
        } else {
            Globals.createEmbed(props.eventChannel, Color.RED, "", "Wrong Syntax");
        }
    }

    public static void removeAuthID(MessageCreateEvent event, CommandProperties props) {
        if (props.params.size() == 1) {
            var id = props.params.get(0);
            if (Authorizer.getAuthorizationLevel(event, Main.authorizations) > Authorizer.getAuthorizationLevel(id,
                    Main.authorizations)) {
                if (Authorizer.deleteAuthorization(id, Main.authorizations, Main.authFilepath)) {
                    Globals.createEmbed(props.eventChannel, Color.MAGENTA, "",
                            "Removed the Authorization Level of " + id + ".");
                }

            } else {
                Globals.createEmbed(props.eventChannel, Color.RED, "",
                        "Your Authorization Level is not high enough for this Command.");
            }
        } else {
            Globals.createEmbed(props.eventChannel, Color.RED, "",
                    "Wrong Syntax, try " + Main.prefix + "removeauthid <id>");
        }
    }

    public static void getAllAuthLvl(MessageCreateEvent event, CommandProperties props) {
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 2) {
            var mssg = "";
            for (var authID : Main.authorizations) {
                mssg += authID.id + "  :  " + authID.level + "\n";
            }
            Globals.createEmbed(props.eventChannel, Color.MAGENTA, "All registered authIDs:", mssg);
        }
    }

    public static String convertAuthLeveltoLevelName(int authlevel) {
        switch (authlevel) {
            case 0:
                return "User";
            case 1:
                return "Moderator";
            case 2:
                return "Admin";
            case 4:
                return "Developer 😎";
            case 10:
                return "Owner";
            default:
                return "Unknown Authorization";
        }
    }

}
