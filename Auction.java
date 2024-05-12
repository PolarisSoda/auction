import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text. *;
import java.util. *;

import javax.swing.text.html.HTMLDocument.HTMLReader.PreAction;

public class Auction {
	private static Scanner scanner = new Scanner(System.in);
	private static Random rand = new Random();
	private static String username;
	private static Connection conn;

	enum Category {
		ELECTRONICS, 
		BOOKS,
		HOME,
		CLOTHING,
		SPORTINGGOODS,
		OTHERS
	}
	enum Condition {
		NEW,
		LIKE_NEW,
		GOOD,
		ACCEPTABLE
	}

	private static String GetNewID(char identifier) {
		String ret;
		while(true) {
			ret = String.valueOf(identifier);
			for(int i=0; i<9; i++) {
				int temp = rand.nextInt(26);
				char c = (char)('a' + temp);
				ret += c;
			}
			try {
				ResultSet rset;
				PreparedStatement pstmt;
				if(identifier == 'I') pstmt = conn.prepareStatement("select * from item_info where item_id = ?");
				else pstmt = conn.prepareStatement("select * from bid_info where bid_id = ?");
				
				pstmt.setString(1,ret);
				rset = pstmt.executeQuery();
				if(!rset.next()) {pstmt.close(); return ret;}
				pstmt.close();
			} catch(SQLException e) {
				System.out.println("SQLException : " + e);	
				System.exit(1);
			}
		}
	}

	private static boolean LoginMenu() {
		String userpass;
		System.out.print("----< User Login >\n");
		System.out.print(" ** To go back, enter 'back' in user ID.\n");
		System.out.print("     user ID: ");

		try {
			username = scanner.next();
			scanner.nextLine();
			if(username.equalsIgnoreCase("back")) return false;
			System.out.print("     password: ");
			userpass = scanner.next();
			scanner.nextLine();
		} catch(java.util.InputMismatchException e) {
			System.out.println("Error: Invalid input is entered. Try again.");
			username = null;
			return false;
		}

		boolean login_success = false; 
		try {
			PreparedStatement pstmt = conn.prepareStatement("select user_id,password from user_info where user_id = ? and password = ?");
			pstmt.setString(1,username);
			pstmt.setString(2,userpass);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) login_success = true;
			pstmt.close();
		} catch(SQLException e) {
			System.out.println("SQLException : " + e);	
			System.exit(1);
		}
		
		if(!login_success) {  
			System.out.println("Error: Incorrect user name or password");
			username = null;
			return false; 
		}
		System.out.println("You are successfully logged in.\n");
		return true;
	}

	private static boolean SignupMenu() {
		String new_username, userpass, isAdmin;
		System.out.print("----< Sign Up >\n");
		System.out.print(" ** To go back, enter 'back' in user ID.\n");
		System.out.print("---- user name: ");

		try {
			new_username = scanner.next();
			scanner.nextLine();
			if(new_username.equalsIgnoreCase("back")) return false;
			if(new_username.equals("any")) { System.out.println("Error: 'any' is not allowed to be username"); return false; }

			System.out.print("---- password: ");
			userpass = scanner.next();
			scanner.nextLine();

			System.out.print("---- In this user an administrator? (Y/N): ");
			isAdmin = scanner.next();
			scanner.nextLine();
		} catch(java.util.InputMismatchException e) {
			System.out.println("Error: Invalid input is entered. Please select again.\n");
			return false;
		}
		
		// Admin is available only at input is Y or y.
		try {
			PreparedStatement pstmt = conn.prepareStatement("select * from user_info where user_id = ?");
			pstmt.setString(1,new_username);
			ResultSet rset = pstmt.executeQuery();
			if(rset.next()) {
				System.out.println("Error: Already using username.\n");
				pstmt.close();
				return false;
			}
			pstmt.close();

			pstmt = conn.prepareStatement("insert into user_info values(?, ?, ?)");
			pstmt.setString(1,new_username);
			pstmt.setString(2,userpass);
			pstmt.setString(3,isAdmin.equalsIgnoreCase("Y") ? "Admin" : "User");
			pstmt.executeUpdate();
			pstmt.close();
		} catch(SQLException e) {
			System.out.println("SQLException : " + e);	
			System.exit(1);
		}
		System.out.println("Your account has been successfully created.\n");
		return true;
	}

	private static boolean SellMenu() {
		Category category = Category.ELECTRONICS; //초기값
		Condition condition = Condition.NEW; //초기값
		String description;
		int price;
		LocalDateTime dateTime, postTime;

		char choice;
		boolean flag_catg = true, flag_cond = true;

		do {
			System.out.print("----< Sell Item >\n");
			System.out.print("---- Choose a category.\n");
			System.out.print("    1. Electronics\n");
			System.out.print("    2. Books\n");
			System.out.print("    3. Home\n");
			System.out.print("    4. Clothing\n");
			System.out.print("    5. Sporting Goods\n");
			System.out.print("    6. Other Categories\n");
			System.out.println("    P. Go Back to Previous Menu");

			try {
				choice = scanner.next().charAt(0);
			} catch(java.util.InputMismatchException e) {
				System.out.println("Error: Invalid input is entered. Try again.");
				continue;
			}

			flag_catg = true;
			switch(choice) {
				case '1':
					category = Category.ELECTRONICS;
					continue;
				case '2':
					category = Category.BOOKS;
					continue;
				case '3':
					category = Category.HOME;
					continue;
				case '4':
					category = Category.CLOTHING;
					continue;
				case '5':
					category = Category.SPORTINGGOODS;
					continue;
				case '6':
					category = Category.OTHERS;
					continue;
				case 'p':
				case 'P':
					return false;
				default:
					System.out.println("Error: Invalid input is entered. Try again.");
					flag_catg = false;
					continue;
			}
		} while(!flag_catg);

		do {
			System.out.print("---- Select the condition of the item to sell.\n");
			System.out.print("   1. New\n");
			System.out.print("   2. Like-new\n");
			System.out.print("   3. Used (Good)\n");
			System.out.print("   4. Used (Acceptable)\n");
			System.out.println("   P. Go Back to Previous Menu");

			try {
				choice = scanner.next().charAt(0);
				scanner.nextLine();
			} catch(java.util.InputMismatchException e) {
				System.out.println("Error: Invalid input is entered. Try again.");
				continue;
			}

			flag_cond = true;
			switch(choice) {
				case '1':
					condition = Condition.NEW;
					break;
				case '2':
					condition = Condition.LIKE_NEW;
					break;
				case '3':
					condition = Condition.GOOD;
					break;
				case '4':
					condition = Condition.ACCEPTABLE;
					break;
				case 'p':
				case 'P':
					return false;
				default:
					System.out.println("Error: Invalid input is entered. Try again.");
					flag_cond = false;
					continue;
			}
		} while(!flag_cond);

		try {
			System.out.println("---- Description of the item (one line): ");
			description = scanner.nextLine();

			System.out.println("---- Buy-It-Now price: ");
			while (!scanner.hasNextInt()) {
				scanner.next();
				System.out.println("Invalid input is entered. Please enter Buy-It-Now price: ");
			}
			price = scanner.nextInt();
			scanner.nextLine();
			
			System.out.print("---- Bid closing date and time (YYYY-MM-DD HH:MM): ");
			String date = scanner.nextLine();  
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			dateTime = LocalDateTime.parse(date, formatter);
		} catch (Exception e) {
			System.out.println("Error: Invalid input is entered. Going back to the previous menu.");
			return false;
		}
		postTime = LocalDateTime.now();

		//item_id,seller_id,category,condition,description,bin_price,date_posted,date_expire
		try {
			String new_id = GetNewID('I');
			PreparedStatement pstmt = conn.prepareStatement("insert into item_info values(?,?,?,?,?,?,?,?)");
			pstmt.setString(1,new_id);
			pstmt.setString(2,username);
			pstmt.setString(3,category.toString());
			pstmt.setString(4,condition.toString());
			pstmt.setString(5,description);
			pstmt.setInt(6,price);
			pstmt.setTimestamp(7,Timestamp.valueOf(postTime));
			pstmt.setTimestamp(8,Timestamp.valueOf(dateTime));
			pstmt.executeUpdate();
			pstmt.close();
		} catch(SQLException e) {
			System.out.println("SQLException : " + e);	
			System.exit(1);
		}
		
		System.out.println("Your item has been successfully listed.\n");
		return true;
	}

	public static boolean BuyItem(){
		Category category = Category.ELECTRONICS;
		Condition condition = Condition.NEW;
		int price;
		String keyword, seller, s_category = "%", s_condition = "%";
		LocalDateTime datePosted;

		char choice;
		boolean flag_catg = true, flag_cond = true;

		do {
			System.out.print("----< Select category > : \n");
			System.out.print("    1. Electronics\n");
			System.out.print("    2. Books\n");
			System.out.print("    3. Home\n");
			System.out.print("    4. Clothing\n");
			System.out.print("    5. Sporting Goods\n");
			System.out.print("    6. Other categories\n");
			System.out.print("    7. Any category\n");
			System.out.println("    P. Go Back to Previous Menu");

			try {
				choice = scanner.next().charAt(0);
				scanner.nextLine();
			} catch (java.util.InputMismatchException e) {
				System.out.println("Error: Invalid input is entered. Try again.");
				return false;
			}

			flag_catg = true;
			switch (choice) {
				case '1':
					category = Category.ELECTRONICS;
					break;
				case '2':
					category = Category.BOOKS;
					break;
				case '3':
					category = Category.HOME;
					break;
				case '4':
					category = Category.CLOTHING;
					break;
				case '5':
					category = Category.SPORTINGGOODS;
					break;
				case '6':
					category = Category.OTHERS;
					break;
				case '7':
					break;
				case 'p':
				case 'P':
					return false;
				default:
					System.out.println("Error: Invalid input is entered. Try again.");
					flag_catg = false;
					continue;
			}
			if(choice != '7') s_category = category.toString();
			else s_category = "%";
		} while(!flag_catg);

		do {
			System.out.print("----< Select the condition > \n");
			System.out.print("   1. New\n");
			System.out.print("   2. Like-new\n");
			System.out.print("   3. Used (Good)\n");
			System.out.print("   4. Used (Acceptable)\n");
			System.out.println("   P. Go Back to Previous Menu");

			try {
				choice = scanner.next().charAt(0);
				scanner.nextLine();
			} catch (java.util.InputMismatchException e) {
				System.out.println("Error: Invalid input is entered. Try again.");
				return false;
			}

			flag_cond = true;
			switch (choice) {
				case '1':
					condition = Condition.NEW;
					break;
				case '2':
					condition = Condition.LIKE_NEW;
					break;
				case '3':
					condition = Condition.GOOD;
					break;
				case '4':
					condition = Condition.ACCEPTABLE;
					break;
				case 'p':
				case 'P':
					return false;
				default:
					System.out.println("Error: Invalid input is entered. Try again.");
					flag_cond = false;
					continue;
			}
			s_condition = condition.toString();
		} while(!flag_cond);

		try {
			System.out.println("---- Enter keyword to search the description : ");
			keyword = scanner.next();
			keyword = "%" + keyword + "%";
			scanner.nextLine();

			System.out.println("---- Enter Seller ID to search : ");
			System.out.println(" ** Enter 'any' if you want to see items from any seller. ");
			seller = scanner.next();
			if(seller.equals("any")) seller = "%";
			scanner.nextLine();

			System.out.println("---- Enter date posted (YYYY-MM-DD): ");
			System.out.println(" ** This will search items that have been posted after the designated date.");
			String date = scanner.next() + " 00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			datePosted = LocalDateTime.parse(date, formatter);
			scanner.nextLine();
		} catch (java.util.InputMismatchException e) {
			System.out.println("Error: Invalid input is entered. Try again.");
			return false;
		}

		//category,conditon,description,seller_id,date_posted
		//item_id,seller_id,category,condition,description,bin_price,date_posted,date_expire,bid_id,buyer_id,bid_posted,price
		LocalDateTime now_time = LocalDateTime.now();
		ArrayList<String> items = new ArrayList<String>();
		ArrayList<String> binps = new ArrayList<String>();
		ArrayList<String> hbids = new ArrayList<String>();

		try {
			String IQ = (
				"select * " +
				"from item_info natural left outer join bid_info " +
				"where category like ? and condition = ? and description like ? and seller_id like ? and date_posted >= ? and item_id not in (select item_id from billing_info)"
			);
			String Q = String.format("select * from (%s) as A order by item_id ASC,price DESC,bid_posted ASC",IQ);
		    
			PreparedStatement pstmt = conn.prepareStatement(Q);
			pstmt.setString(1,s_category);
			pstmt.setString(2,s_condition);
			pstmt.setString(3,keyword);
			pstmt.setString(4,seller);
			pstmt.setTimestamp(5,Timestamp.valueOf(datePosted));
			ResultSet rset = pstmt.executeQuery();

			String prev = "nope";
			System.out.println("Item ID | Item description | Condition | Seller | Buy-It-Now | Current Bid | highest bidder | Time left | bid close");
			System.out.println("-------------------------------------------------------------------------------------------------------");
			while(rset.next()) {
				String arr[] = new String[9];
				arr[0] = rset.getString(1); //item_id
				arr[1] = rset.getString(5); //item_description
				arr[2] = rset.getString(4); //item_condition
				arr[3] = rset.getString(2); //item_seller
				arr[4] = rset.getString(6); //item_bin_price
				arr[5] = rset.getString(12) == null ? "-" : rset.getString(12); //item_current_bid
				arr[6] = rset.getString(10) == null ? "-" : rset.getString(10); //highest_bidder
				Long interval = rset.getTimestamp(8).getTime() - Timestamp.valueOf(now_time).getTime();
				interval *= 10000;
				LocalDateTime triggerTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(interval),TimeZone.getDefault().toZoneId());
				arr[7] = triggerTime.toString(); //time_left
				arr[8] = rset.getTimestamp(8).toString(); //bid_close
				System.out.println("HELLO");
				if(arr[0].equals(prev)) continue; //이전과 같은 ID인가?
				prev = arr[0];
				if(arr[3].equals(username)) continue; //현재 user가 올린 item인가?
				if(rset.getTimestamp(8).before(Timestamp.valueOf(now_time))) continue; //아 이미 끝나셨어?
				System.out.printf("%s | %-16s | %-16s | %-16s | %-12s | %-12s | %-16s | %-12s | %s\n",arr[0],arr[1],arr[2],arr[3],arr[4],arr[5],arr[6],arr[7],arr[8]);
				items.add(arr[0]); binps.add(arr[4]); hbids.add(arr[5]);
			}
			pstmt.close();
		} catch(SQLException e) {
			System.out.println("SQLException : " + e);	
			System.exit(1);
		}

		System.out.println("---- Select Item ID to buy or bid: ");
		String selected_item;
		try {
			selected_item = scanner.next();
			scanner.nextLine();
			System.out.println("     Price: ");
			price = scanner.nextInt();
			scanner.nextLine();
		} catch (java.util.InputMismatchException e) {
			System.out.println("Error: Invalid input is entered. Try again.");
			return false;
		}
		now_time = LocalDateTime.now().withNano(3);

		boolean found = false;
		int idx = 0;
		for(idx=0; idx<items.size(); idx++) {
			if(items.get(idx).equals(selected_item)) {
				found = true;
				break;
			}
		}
		if(found == false) {System.out.println("Error: "); return false;}

		try {
			int bin = Integer.valueOf(binps.get(idx));
			if(price >= bin) {
				PreparedStatement pstmt = conn.prepareStatement("insert into billing_info values(?,?,?,?)");
				pstmt.setString(1,items.get(idx)); //item_id
				pstmt.setString(2,username); //buyer_id
				pstmt.setTimestamp(3,Timestamp.valueOf(now_time));//purchased_date
				pstmt.setInt(4,bin);
				pstmt.executeUpdate();
				pstmt.close();
				System.out.println("Congratulations, the item is yours now.\n"); 
			} else {
				PreparedStatement pstmt = conn.prepareStatement("insert into bid_info values(?,?,?,?,?)");
				pstmt.setString(1,GetNewID('B')); //bid_id
				pstmt.setString(2,items.get(idx)); //item_id
				pstmt.setString(3,username); //buyer_id
				pstmt.setTimestamp(4,Timestamp.valueOf(now_time)); //bid_posted
				pstmt.setInt(5,price); //price
				pstmt.executeUpdate();
				pstmt.close();
				if(price > Integer.valueOf(hbids.get(idx))) System.out.println("Congratulations, you are the highest bidder.\n"); 
			}
		} catch(SQLException e) {
			System.out.println("SQLException : " + e);	
			System.exit(1);
		}
		return true;
	}

	private static boolean AdminMenu() {
		char choice;
		String adminname, adminpass;
		String keyword, seller;
		System.out.print("----< Login as Administrator >\n");
		System.out.print(" ** To go back, enter 'back' in user ID.\n");
		System.out.print("---- admin ID: ");

		try {
			adminname = scanner.next();
			scanner.nextLine();
			if(adminname.equalsIgnoreCase("back")) return false;
			System.out.print("---- password: ");
			adminpass = scanner.nextLine();
		} catch(java.util.InputMismatchException e) {
			System.out.println("Error: Invalid input is entered. Try again.");
			return false;
		}

		boolean login_success = false; 
		try {
			PreparedStatement pstmt = conn.prepareStatement("select user_id,password from user_info where user_id = ? and password = ? and role = 'Admin'");
			pstmt.setString(1,adminname);
			pstmt.setString(2,adminpass);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) login_success = true;
			pstmt.close();
		} catch(SQLException e) {
			System.out.println("SQLException : " + e);	
			System.exit(1);
		}
		if(!login_success) {  
			System.out.println("Error: Incorrect user name or password");
			return false; 
		}

		//REAL ADMIN MENU BELOW

		do {
			System.out.println(
					"----< Admin menu > \n" +
					"    1. Print Sold Items per Category \n" +
					"    2. Print Account Balance for Seller \n" +
					"    3. Print Seller Ranking \n" +
					"    4. Print Buyer Ranking \n" +
					"    P. Go Back to Previous Menu"
					);

			try {
				choice = scanner.next().charAt(0);;
				scanner.nextLine();
			} catch (java.util.InputMismatchException e) {
				System.out.println("Error: Invalid input is entered. Try again.");
				continue;
			}

			if (choice == '1') {
				System.out.println("----Enter Category to search : ");
				keyword = scanner.next();
				scanner.nextLine();
				/*TODO: Print Sold Items per Category */
				System.out.println("sold item       | sold date       | seller ID   | buyer ID   | price | commissions");
				System.out.println("----------------------------------------------------------------------------------");
				/*
				   while(rset.next()){
				   }
				 */
				continue;
			} else if (choice == '2') {
				/*TODO: Print Account Balance for Seller */
				System.out.println("---- Enter Seller ID to search : ");
				seller = scanner.next();
				scanner.nextLine();
				System.out.println("sold item       | sold date       | buyer ID   | price | commissions");
				System.out.println("--------------------------------------------------------------------");
				/*
				   while(rset.next()){
				   }
				 */
				continue;
			} else if (choice == '3') {
				/*TODO: Print Seller Ranking */
				System.out.println("seller ID   | # of items sold | Total Profit (excluding commissions)");
				System.out.println("--------------------------------------------------------------------");
				/*
				   while(rset.next()){
				   }
				 */
				continue;
			} else if (choice == '4') {
				/*TODO: Print Buyer Ranking */
				System.out.println("buyer ID   | # of items purchased | Total Money Spent ");
				System.out.println("------------------------------------------------------");
				/*
				   while(rset.next()){
				   }
				 */
				continue;
			} else if (choice == 'P' || choice == 'p') {
				return false;
			} else {
				System.out.println("Error: Invalid input is entered. Try again.");
				continue;
			}
		} while(true);
	}

	public static void CheckSellStatus(){
		/* Check the status of the item the current user is selling */
		//전부다 출력해도 상관없다.
		ResultSet rset;
		LocalDateTime now_time = LocalDateTime.now();
		try {
			String Q = "select * from (select item_id from item_info where seller_id like ? and date_expire >= ?) as A natural left outer join bid_info";
			PreparedStatement pstmt = conn.prepareStatement(Q);
			pstmt.setString(1,username);
			pstmt.setTimestamp(2,Timestamp.valueOf(now_time));
			rset = pstmt.executeQuery();

			//item_id bid_id buyer_id bid_posted price

			System.out.println("item listed in Auction | bidder (buyer ID) | bidding price | bidding date/time \n");
			System.out.println("-------------------------------------------------------------------------------\n");
			while(rset.next()) {
				String ni = rset.getString(1);
				String nb = rset.getString(3) == null ? "-" : rset.getString(3);
				String np = rset.getString(4) == null ? "-" : rset.getString(5);
				String nt = rset.getString(4) == null ? "-" : rset.getString(4);
				System.out.printf("%-16s | %-16s | %-16s | %-16s\n",ni,nb,np,nt);
			}
			pstmt.close();
		} catch(SQLException e) {
			System.out.println("SQLException : " + e);	
			System.exit(1);
		}
	}

	public static void CheckBuyStatus(){
		/* TODO: Check the status of the item the current buyer is bidding on */
		/* Even if you are outbidded or the bid closing date has passed, all the items this user has bidded on must be displayed */
		ResultSet rset;
		try {
			//select distinct item_id from bid_info where buyer_id = ?; //실제 필요한 id.
			//select * from bid_info where item_id in (select distinct item_id from bid_info where buyer_id = 'user3') order by item_id ASC,price DESC, bid_posted ASC;
			//select * from item_info natural join (select item_id,max(price) as user_bid from bid_info where buyer_id = 'user3' group by item_id) as A;
			String AQ = "select item_id,max";
			String IQ = "(select item_id,max(price) as user_bid from bid_info where buyer_id = ? group by item_id)"; //현재 유저가 참여한 경매 목록과 그것의 최대 bid.
			PreparedStatement pstmt = conn.prepareStatement(IQ);
			rset = pstmt.executeQuery();
			System.out.println("item ID   | item description   | highest bidder | highest bidding price | your bidding price | bid closing date/time");
			System.out.println("--------------------------------------------------------------------------------------------------------------------");
			pstmt.close();
		} catch(SQLException e) {
			System.out.println("SQLException : " + e);
			System.exit(1);
		}
		
		/*
		   while(rset.next(){
		   System.out.println();
		   }
		 */
	}

	public static void CheckAccount(){
		/* TODO: Check the balance of the current user.  */
		System.out.println("[Sold Items] \n");
		System.out.println("item category  | item ID   | sold date | sold price  | buyer ID | commission  ");
		System.out.println("------------------------------------------------------------------------------");
		/*
		   while(rset.next(){
		   System.out.println();
		   }
		 */
		System.out.println("[Purchased Items] \n");
		System.out.println("item category  | item ID   | purchased date | puchased price  | seller ID ");
		System.out.println("--------------------------------------------------------------------------");
		/*
		   while(rset.next(){
		   System.out.println();
		   }
		 */
	}

	public static void main(String[] args) {
		char choice;
		boolean ret;

		if(args.length < 2) {
			System.out.println("Usage: java Auction postgres_id password");
			System.exit(1);
		}
		
		try {
            //conn = DriverManager.getConnection("jdbc:postgresql://localhost/"+args[0], args[0], args[1]); 
            Auction.conn = DriverManager.getConnection("jdbc:postgresql://localhost/s20311486", "s20311486", "urin1223%");
		} catch(SQLException e) {
			System.out.println("SQLException : " + e);	
			System.exit(1);
		}

		do {
			username = null;
			System.out.println(
					"----< Login menu >\n" + 
					"----(1) Login\n" +
					"----(2) Sign up\n" +
					"----(3) Login as Administrator\n" +
					"----(Q) Quit"
					);

			try {
				choice = scanner.next().charAt(0);;
				scanner.nextLine();
			} catch(java.util.InputMismatchException e) {
				System.out.println("Error: Invalid input is entered. Try again.");
				continue;
			}

			try {
				switch((int)choice) {
					case '1':
						ret = LoginMenu();
						if(!ret) continue;
						break;
					case '2':
						ret = SignupMenu();
						if(!ret) continue;
						break;
					case '3':
						ret = AdminMenu();
						if(!ret) continue;
					case 'q':
					case 'Q':
						System.out.println("Good Bye");
						/* TODO: close the connection and clean up everything here */
						conn.close();
						System.exit(1);
					default:
						System.out.println("Error: Invalid input is entered. Try again.");
				}
			} catch (SQLException e) {
				System.out.println("SQLException : " + e);	
			}
		} while(username==null || username.equalsIgnoreCase("back"));  

		// logged in as a normal user 
		do {
			System.out.println(
					"---< Main menu > :\n" +
					"----(1) Sell Item\n" +
					"----(2) Status of Your Item Listed on Auction\n" +
					"----(3) Buy Item\n" +
					"----(4) Check Status of your Bid \n" +
					"----(5) Check your Account \n" +
					"----(Q) Quit"
					);

			try {
				choice = scanner.next().charAt(0);;
				scanner.nextLine();
			} catch(java.util.InputMismatchException e) {
				System.out.println("Error: Invalid input is entered. Try again.");
				continue;
			}

			try {
				switch(choice) {
					case '1':
						ret = SellMenu();
						if(!ret) continue;
						break;
					case '2':
						CheckSellStatus();
						break;
					case '3':
						ret = BuyItem();
						if(!ret) continue;
						break;
					case '4':
						CheckBuyStatus();
						break;
					case '5':
						CheckAccount();
						break;
					case 'q':
					case 'Q':
						System.out.println("Good Bye");
						/* close the connection and clean up everything here */
						conn.close();
						System.exit(1);
				}
			} catch(SQLException e) {
				System.out.println("SQLException : " + e);	
				System.exit(1);
			}
		} while(true);
	} // End of main 
} // End of class


