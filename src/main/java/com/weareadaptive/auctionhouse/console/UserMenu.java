package com.weareadaptive.auctionhouse.console;

import com.weareadaptive.auctionhouse.StringUtil;
import com.weareadaptive.auctionhouse.model.BusinessException;
import com.weareadaptive.auctionhouse.model.User;

public class UserMenu extends ConsoleMenu {

    @Override
    public void display(MenuContext context) {
        createMenu(
                context,
                option("Create new user", this::createUser),
                option("Get all users", this::getAllUsers),
                option("Get all organisations", this::getAllOrganisations),
                option("Get user details", this::getUserDetails),
                option("Block / unblock user accounts", this::testFn),
                leave("Go Back")
                );
    }

    private void testFn (MenuContext context) {
        var out = context.getOut();
        out.println("Test function");
    }
    private void createUser(MenuContext context) {
        var out = context.getOut();
        var scanner = context.getScanner();
        var state = context.getState();

        out.println("Enter the username:");
        final var username = scanner.nextLine();

        var usernameExists = state.userState().doesUsernameExist(username);

        if (usernameExists) {
            out.println("Username already exists");
            // TODO: Investigate if I can make the user just go back to the username input
            return;
        }

        if (!StringUtil.isAlphanumeric(username)){
            out.printf("Invalid username format: %s. %nCan only contain alphanumeric characters and no spaces. %n", username);
            // TODO: Investigate if I can make the user just go back to the username input
            return;
        }

        out.println("Enter the password:");
        var password = readPassword(scanner);

        // Enter password a second time
        out.println("Repeat the password:");
        var secondPassword = readPassword(scanner);

        if (!password.equals(secondPassword)) {
            out.println("Password has to be the same");
            // TODO: Investigate if I can make the user just go back to the password input
            return;
        }

        out.println("Enter the first name:");
        var firstName = scanner.nextLine();

        out.println("Enter the last name:");
        var lastName = scanner.nextLine();

        out.println("Enter the organization:");
        var organization = scanner.nextLine();

        var newUser = new User(
                state.userState().nextId(),
                username,
                password,
                firstName,
                lastName,
                organization
        );

        // How to catch if something goes wrong in the creation?
        // It's breaking right now

        try {
            state.userState().add(newUser);
        } catch(BusinessException businessException) {
            out.println("Cannot create user.");
            out.println(businessException.getMessage());
        }
    }

    private void getAllUsers(MenuContext context) {
        var out = context.getOut();
        var scanner = context.getScanner();
        out.println("All users ---------->");

        // How should this be formatted?
        // It kinda looks like rxjs, that's fun
        context.getState()
                .userState()
                .stream()
                .forEach(user ->
                        out.printf("Username: %s, Firstname: %s, Lastname: %s, Organisation: %s%n",
                                user.getUsername(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getOrganisation()
                        )
                );

        out.println("Press enter to continue...");
        scanner.nextLine();
    }

    private void getAllOrganisations(MenuContext context) {
        var out = context.getOut();
        var scanner = context.getScanner();
        out.println("Organisations ---------->");

        // Move logic to user state?
        context.getState()
                .userState()
                .stream()
                .map(User::getOrganisation)
                // Remove duplicate organisations
                .distinct()
                .forEach(out::println);

        out.println("Press enter to continue...");
        scanner.nextLine();
    }

    private void getUserDetails(MenuContext context) {
        var out = context.getOut();
        var scanner = context.getScanner();

        // This is displaying the current user's details -> add a dropdown to choose a user and show that one?

        var userOptions = context.getState()
                .userState()
                .stream()
                .map(u ->
                        option(u.getUsername(), () -> out.printf(
                                "Username: %s, %nFirst Name: %s, %nLast Name: %s, %nOrganisation: %s %n",
                                u.getUsername(),
                                u.getFirstName(),
                                u.getLastName(),
                                u.getOrganisation()
                        )))
                .toList();

        // TODO: Accessing the first option works, but I can't figure out how to spread the options when passing it to the menu
        createMenu(
                context,
                userOptions.get(0),
                leave("Go back")
        );
    }

    // TODO: Block / unblock user
}
