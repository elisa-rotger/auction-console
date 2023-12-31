package com.weareadaptive.auctionhouse.console;

public class LoginMenu extends ConsoleMenu {
    private final AuctionMenu auctionMenu;
    private final UserMenu userMenu;

    public LoginMenu() {
        // Two different menus -> 2 classes. 1 auction management, 1 user management (for admin users)
        auctionMenu = new AuctionMenu();
        userMenu = new UserMenu();
    }

    // Overrides the parent's 'display' method
    @Override
    public void display(MenuContext context) {
        createMenu(
                context,
                option("Login", this::login),
                leave("Quit")
        );
    }

  private void login(MenuContext context) {
    var out = context.getOut();

    out.println("Enter your username:");
    var username = context.getScanner().nextLine();

    out.println("Enter your password:");
    var password = readPassword(context.getScanner());

    context.getState()
        .userState()
        .findUserByUsernameAndPassword(username, password)
        .ifPresentOrElse(user -> {
          if (user.getIsBlocked()) {
              out.printf("User %s has been blocked. %n", user.getUsername());
              pressEnter(context);
              return;
          }
          context.setCurrentUser(user);
          out.printf("Welcome %s %s %n", user.getFirstName(), user.getLastName());
          createMenu(
                  context,
                  option("User management", userMenu::display, u -> user.isAdmin()),
                  option("Auction management", auctionMenu::display),
                  leave("Log out")
          );
        }, () -> out.println("Invalid username/password combination"));
  }
}
