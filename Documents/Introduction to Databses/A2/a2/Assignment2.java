/* 
 * This code is provided solely for the personal and private use of students 
 * taking the CSC343H course at the University of Toronto. Copying for purposes 
 * other than this use is expressly prohibited. All forms of distribution of 
 * this code, including but not limited to public repositories on GitHub, 
 * GitLab, Bitbucket, or any other online platform, whether as given or with 
 * any changes, are expressly prohibited. 
*/ 

import java.sql.*;
import java.util.Date;
import java.util.Arrays;
import java.util.List;

public class Assignment2 {
   /////////
   // DO NOT MODIFY THE VARIABLE NAMES BELOW.
   
   // A connection to the database
   Connection connection;

   // Can use if you wish: seat letters
   List<String> seatLetters = Arrays.asList("A", "B", "C", "D", "E", "F");

   Assignment2() throws SQLException {
      try {
         Class.forName("org.postgresql.Driver");
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      }
   }

  /**
   * Connects and sets the search path.
   *
   * Establishes a connection to be used for this session, assigning it to
   * the instance variable 'connection'.  In addition, sets the search
   * path to 'air_travel, public'.
   *
   * @param  url       the url for the database
   * @param  username  the username to connect to the database
   * @param  password  the password to connect to the database
   * @return           true if connecting is successful, false otherwise
   */
   public boolean connectDB(String URL, String username, String password) {
      try{
        	connection = DriverManager.getConnection(URL, username, password);
		   String queryString = "SET search_path to air_travel,public";
        	PreparedStatement pStatement = connection.prepareStatement(queryString);
        	pStatement.executeUpdate();
		   return true;
      } 
      catch (SQLException e){
         e.printStackTrace();	
         return false;
      }
   }

  /**
   * Closes the database connection.
   *
   * @return true if the closing was successful, false otherwise
   */
   public boolean disconnectDB() {
      try{
         connection.close();
         return true;
      } catch(Exception e){
         e.printStackTrace();
         return false;
      } 
   }
   
   
   /* ======================= Airline-related methods ======================= */

   /**
    * Attempts to book a flight for a passenger in a particular seat class. 
    * Does so by inserting a row into the Booking table.
    *
    * Read handout for information on how seats are booked.
    * Returns false if seat can't be booked, or if passenger or flight cannot be found.
    *
    * 
    * @param  passID     id of the passenger
    * @param  flightID   id of the flight
    * @param  seatClass  the class of the seat (economy, business, or first) 
    * @return            true if the booking was successful, false otherwise. 
    */
   public boolean bookSeat(int passID, int flightID, String seatClass) {
     
      try{
         PreparedStatement ps = connection.prepareStatement("SELECT flight.id, plane.capacity_economy as economy_cap, plane.capacity_business as business_cap, plane.capacity_first as first_cap " + "FROM flight, plane " + "WHERE flight.plane = plane.tail_number " + "and flight.id = ?");
         ps.setInt(1, flightID);
         ResultSet flight_capacity = ps.executeQuery();

         PreparedStatement flight_price = connection.prepareStatement("SELECT * FROM price WHERE flight_id = ?");
         flight_price.setInt(1, flightID);
         ResultSet res_price = flight_price.executeQuery();

         PreparedStatement books = connection.prepareStatement("SELECT count(*) " + "FROM booking " + "WHERE booking.flight_id = ? and seat_class = ?::seat_class ");
	      books.setInt(1, flightID);
	      books.setString(2, seatClass);
         ResultSet already_booked = books.executeQuery();

         while(flight_capacity.next() && res_price.next() && already_booked.next()){
            int price = res_price.getInt(seatClass);

////////////////////////////////////////////////////////////////// START FIRST//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            if(seatClass == "first"){
               if(flight_capacity.getInt("first_cap") - already_booked.getInt("count")> 0){
                  PreparedStatement ps_first = connection.prepareStatement("INSERT INTO booking " + "VALUES((SELECT MAX(id) FROM booking)+1, ?, ?, ?, ?, ?::seat_class, ?, ?)" );
			         ps_first.setInt(1, passID);
			         ps_first.setInt(2, flightID);
			         ps_first.setTimestamp(3, getCurrentTimeStamp());
			         ps_first.setInt(4, price);
			         ps_first.setString(5, seatClass);
			         int first_start = 1;		
			         int max_row = first_start + already_booked.getInt("count")/6;
			         int max_letter_num = already_booked.getInt("count") % 6;
                  char max_letter = 'A';
                  if(max_letter_num != 0)
					         max_letter = (char)(max_letter + max_letter_num);
                  if(max_letter_num == 0)
                     max_row += 1;
                  ps_first.setInt(6, max_row);
			         ps_first.setString(7, max_letter+" ");
			         ps_first.setString(7, String.valueOf(max_letter));
			         ps_first.executeUpdate();
			         return true;
               }
               return false;
            }
////////////////////////////////////////////////////////////////// END FIRST//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////// START BUSIN//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            if(seatClass == "business"){
               if(flight_capacity.getInt("business_cap") - already_booked.getInt("count")> 0){
                  PreparedStatement ps_busin = connection.prepareStatement("INSERT INTO booking " + "VALUES((SELECT MAX(id) FROM booking)+1, ?, ?, ?, ?, ?::seat_class, ?, ?)" );
			         ps_busin.setInt(1, passID);
			         ps_busin.setInt(2, flightID);
			         ps_busin.setTimestamp(3, getCurrentTimeStamp());
			         ps_busin.setInt(4, price);
			         ps_busin.setString(5, seatClass);
                  int busin_start = flight_capacity.getInt("first_cap")/6 + 1 + 1;		
			         int max_row = busin_start + already_booked.getInt("count")/6;
			         int max_letter_num = already_booked.getInt("count") % 6;
                  char max_letter = 'A';
                  if(max_letter_num != 0)
					         max_letter = (char)(max_letter + max_letter_num);
                  if(max_letter_num == 0)
                     max_row += 1;
                  ps_busin.setInt(6, max_row);
			         ps_busin.setString(7, max_letter+" ");
			         ps_busin.setString(7, String.valueOf(max_letter));
			         ps_busin.executeUpdate();
			         return true;
               }
               return false;
            }
////////////////////////////////////////////////////////////////// END BUSIN//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////// START ECON//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////            
           
            if(seatClass == "economy"){
               if(flight_capacity.getInt("economy_cap") - already_booked.getInt("count") > -10){
                  PreparedStatement ps_eco = connection.prepareStatement("INSERT INTO booking " + "VALUES((SELECT MAX(id) FROM booking)+1, ?, ?, ?, ?, ?::seat_class, ?, ?)" );
			         ps_eco.setInt(1, passID);
			         ps_eco.setInt(2, flightID);
			         ps_eco.setTimestamp(3, getCurrentTimeStamp());
			         ps_eco.setInt(4, price);
			         ps_eco.setString(5, seatClass);

                  if(flight_capacity.getInt("economy_cap") - already_booked.getInt("count") > 0){
                     int eco_start = flight_capacity.getInt("first_cap")/6 + 1 + flight_capacity.getInt("business_cap")/6 + 1 + 1;		
				         int max_row = eco_start + already_booked.getInt("count")/6;
				         int max_letter_num = already_booked.getInt("count") % 6;
                     char max_letter = 'A';
                     if(max_letter_num != 0)
					         max_letter = (char)(max_letter + max_letter_num);
                     if(max_letter_num == 0)
                        max_row += 1;
                     ps_eco.setInt(6, max_row);
				         ps_eco.setString(7, max_letter+" ");
				         ps_eco.setString(7, String.valueOf(max_letter));
				      }
                  else{
                     ps_eco.setNull(6, Types.NULL);
                   	ps_eco.setNull(7, Types.NULL);
                  }
                  ps_eco.executeUpdate();
                  return true;
               }
               return false;
            }
////////////////////////////////////////////////////////////////// END ECON//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////      
         }
      }
      catch(Exception e){
         e.printStackTrace();
         return false;
      }
      return false;
   }

   /**
    * Attempts to upgrade overbooked economy passengers to business class
    * or first class (in that order until each seat class is filled).
    * Does so by altering the database records for the bookings such that the
    * seat and seat_class are updated if an upgrade can be processed.
    *
    * Upgrades should happen in order of earliest booking timestamp first.
    *
    * If economy passengers are left over without a seat (i.e. more than 10 overbooked passengers or not enough higher class seats), 
    * remove their bookings from the database.
    * 
    * @param  flightID  The flight to upgrade passengers in.
    * @return           the number of passengers upgraded, or -1 if an error occured.
    */
   public int upgrade(int flightID) {
      // Implement this method!
      int pplUpgradedBussiness;
      int pplUpgradedFirstClass;
    try{

      // GLOBALS // 

          // Used First Class seats //
        
      PreparedStatement firstClassData = connection.prepareStatement("SELECT flight_id,count(id),max(row) AS maxRow "+"FROM Booking "+"WHERE flight_id = ? AND seat_class='first' "+"GROUP BY flight_id");
        firstClassData.setInt(1, flightID);
      
      ResultSet occupiedFC = firstClassData.executeQuery();

        // Used Business Class seats //

      PreparedStatement businessClassData = connection.prepareStatement("SELECT flight_id,count(id),max(row) AS maxRow " + "FROM Booking " + "WHERE flight_id = ? AND seat_class = 'business' "+"GROUP BY flight_id");
        businessClassData.setInt(1, flightID);
      
      ResultSet occupiedBC = businessClassData.executeQuery();

        // Tuples that need modification // 

      PreparedStatement overBooked = connection.prepareStatement("SELECT id " + "FROM Booking " + "WHERE flight_id = ? AS seat_class = 'economy' AND row IS NULL AND letter IS NULL " + "ORDER BY datetime");
        overBooked.setInt(1, flightID);
      
      ResultSet ppl2Upgrade = overBooked.executeQuery();

         pplUpgradedBussiness = 0;
         pplUpgradedFirstClass = 0;


     // Checking if the flight is valid // 

      PreparedStatement flights = connection.prepareStatement("SELECT count(flight_id) AS numFlights "+"FROM Booking "+"WHERE flight_id = ?");
      flights.setInt(1,flightID);
      ResultSet flightID_count = flights.executeQuery();
      int isFlightValid = flightID_count.getInt("numFlights");

     // Check if there is any passangers to update // 

      PreparedStatement pass2update = connection.prepareStatement("SELECT count(*) AS numNullPass "+
        "FROM Booking "+
        "WHERE row IS NULL AND letter is NULL AND seat_class = 'economy' AND flight_id = ?");
      pass2update.setInt(1,flightID);
      ResultSet nullPass_count = pass2update.executeQuery();
      int numNullPass = nullPass_count.getInt("numNullPass");
      
     // Check if flight has departed or not // 

      PreparedStatement flightDeparted = connection.prepareStatement("SELECT count(*) AS isflightDep "+
        "FROM Departure "+
        "WHERE flight_id = ?");
      flightDeparted.setInt(1,flightID);
      ResultSet exec_flightDeparted = flightDeparted.executeQuery();
      int isflightDep = exec_flightDeparted.getInt("isflightDep");

            
 

      
      // isFlightValid == 0 -> FlightNotFound 
      // isFlightValid == 1 -> FlightFound -> Can furhter process 

      if(isFlightValid == 0 || numNullPass == 0 || isflightDep == 1){
        return -1;
      }

      else{

        /*General Approach

        1) Find all the overbooked passangers
        2) Checking if we have enough seats: Find total of bussSeats and firstClassSeats left
          2a) If total = numOverbookedPassangers 
            - Proceed to upgrade by class and timestamp (business class and earliest time stamp first)
            - return numPassangersUpgraded
          2b) If 0 < total < numOverbookedPassangers
            - Book those who you can and remove the rest from the Booking table
            - return numPassangersUpgraded
          2c) If total = 0 
            - Drop all overbooked passangers 
            - return numPassangersUpdgraded*/

        // NUMBER OF OVERBOOKED PASSANGERS//            

        PreparedStatement overBookedPassangers = connection.prepareStatement("SELECT count(*) AS numOverbookedPassangers "+
          "FROM Booking "+
          "WHERE flight_id = ? AND row IS NULL AND letter IS NULL AND seat_class = 'economy'");
        overBookedPassangers.setInt(1,flightID);
        ResultSet overbookedpeople = overBookedPassangers.executeQuery();
        
        

        int numOverbookedPassangers = overbookedpeople.getInt("numOverbookedPassangers");

        // NUMBER OF BUSINESS SEATS LEFT // 

        PreparedStatement cv_BusinessSeatsBooked = connection.prepareStatement("DROP VIEW IF EXISTS  BusinessSeatsBooked CASCADE");
        ResultSet exec_BusinessSeatsBooked = cv_BusinessSeatsBooked.executeQuery();

        PreparedStatement bussinessSeatsBooked = connection.prepareStatement("CREATE VIEW BusinessSeatsBooked AS "+
          "SELECT count(*) AS numBusinessSeatsBooked,plane " +"FROM Booking JOIN Flight ON Booking.flight_id = Flight.id "+
          "WHERE Booking.flight_id = ? AND seat_class = 'business' "+
          "GROUP BY plane");
        bussinessSeatsBooked.setInt(1,flightID);
        ResultSet bussinessSeats = bussinessSeatsBooked.executeQuery();

        PreparedStatement stmt_numBusinessSeatsLeft = connection.prepareStatement("SELECT Plane.capacity_business - numBusinessSeatsBooked AS businessSeatsAvailable "+
          "FROM BusinessSeatsBooked NATURAL JOIN Plane "+
          "WHERE BusinessSeatsBooked.plane = Plane.tail_number");
        ResultSet exec_numBusinessSeatsLeft = stmt_numBusinessSeatsLeft.executeQuery();

        
        int bussinessSeatsAvailable = exec_numBusinessSeatsLeft.getInt("businessSeatsAvailable");  


        // NUMBER OF ECONOMY SEATS LEFT // 

        PreparedStatement cv_EconomySeatsBooked = connection.prepareStatement("DROP VIEW IF EXISTS  EconomySeatsBooked CASCADE");
        ResultSet exec_EconomySeatsBooked = cv_EconomySeatsBooked.executeQuery();

        PreparedStatement economySeatsBooked = connection.prepareStatement("CREATE VIEW EconomySeatsBooked AS "+
          "SELECT count(*) AS numEconomySeatsBooked,plane " +"FROM Booking JOIN Flight ON Booking.flight_id = Flight.id "+
          "WHERE Booking.flight_id = ? AND seat_class = 'economy' "+
          "GROUP BY plane");
        economySeatsBooked.setInt(1,flightID);
        ResultSet economySeats = economySeatsBooked.executeQuery();

        PreparedStatement stmt_numEconomySeatsLeft = connection.prepareStatement("SELECT Plane.capacity_economy - numEconomySeatsBooked AS economySeatsAvailable "+
          "FROM EconomySeatsBooked NATURAL JOIN Plane "+
          "WHERE EconomySeatsBooked.plane = Plane.tail_number");
        ResultSet exec_numEconomySeatsLeft = stmt_numEconomySeatsLeft.executeQuery();

        
        int economySeatsAvailable = exec_numEconomySeatsLeft.getInt("economySeatsAvailable"); 

        // NUMBER OF FIRSTCLASS SEATS LEFT // 

       PreparedStatement cv_FirstSeatsBooked = connection.prepareStatement("DROP VIEW IF EXISTS FirstSeatsBooked CASCADE");
       ResultSet exec_FirstSeatsBooked = cv_FirstSeatsBooked.executeQuery();

       PreparedStatement firstSeatsBooked = connection.prepareStatement("CREATE VIEW FirstSeatsBooked AS "+
        "SELECT count(*) AS numFirstSeatsBooked,plane "+
        "FROM Booking JOIN Flight ON Booking.flight_id = Flight.id "+
        "WHERE Booking.flight_id = ? AND seat_class = 'first' "+
        "GROUP BY plane");

       firstSeatsBooked.setInt(1,flightID);
       ResultSet firstSeats = firstSeatsBooked.executeQuery();

       PreparedStatement stmt_numfirstSeatsLeft = connection.prepareStatement("SELECT Plane.capacity_first - numFirstSeatsBooked AS firstSeatsAvailable "+
        "FROM FirstSeatsBooked NATURAL JOIN Plane "+
        "WHERE FirstSeatsBooked.plane = Plane.tail_number");

       ResultSet exec_numfirstSeatsLeft = stmt_numfirstSeatsLeft.executeQuery();

       int firstSeatsAvailable = exec_FirstSeatsBooked.getInt("firstSeatsAvailable");

       

       int totalSeats = bussinessSeatsAvailable + firstSeatsAvailable;

       
       // Case # 0: economySeatsAvailable > numNullPass //

       if (economySeatsAvailable >= numNullPass){

            return 0;
        }



       // Case # 1: totalSeats = 0 // 

       if(totalSeats == 0){

        // Write Queries to drop all tuples where seat_class = economy, row IS NULL, letter IS NULL AND return 0// 
        PreparedStatement deleteOverBookedEcon = connection.prepareStatement("DELETE "+
          "FROM ONLY Booking "+
          "WHERE flight_id = ? AND seat_class = 'economy' AND row IS NULL AND letter IS NULL");
        deleteOverBookedEcon.setInt(1,flightID);
        ResultSet exec_deleteOverBookedEcon = deleteOverBookedEcon.executeQuery();

        return 0;
       }

       // Case # 2: totalSeats < numOverbookedPassangers // 

       else if (totalSeats < numOverbookedPassangers ){

        /*Write queries to modify overBooked Passangers till we run out of seats; drop the remainaing overbooked Passangers
        Return num of seats upgraded*/ 

       while(ppl2Upgrade.next()){

        if(bussinessSeatsAvailable > 0) {

          PreparedStatement changeQuery = connection.prepareStatement("UPDATE Booking " + "SET seat_class ='business' AND row = ? AND letter = ? " + "WHERE Booking.id = ppl2Upgrade.id");

          int maxRow = occupiedBC.getInt("maxRow");
          int max_letter_num = occupiedBC.getInt("count") % 6;
          char max_letter = 'A';
          
          if(max_letter_num != 0) max_letter = (char)(max_letter + max_letter_num);
        
          if(max_letter_num == 0) maxRow = maxRow + 1;
          changeQuery.setInt(1, maxRow);
          changeQuery.setString(2, max_letter+" ");
          changeQuery.setString(2, String.valueOf(max_letter));
          changeQuery.executeUpdate(); 
  
                bussinessSeatsAvailable--; 
                pplUpgradedBussiness++;     
              
        }




        else if(firstSeatsAvailable > 0){

          PreparedStatement changeQuery = connection.prepareStatement("UPDATE Booking " + "SET seat_class = 'first' AND row = ? AND letter = ? " +  "WHERE Booking.id = ppl2Upgrade.id ");

          int maxRow = occupiedFC.getInt("maxRow");
          int max_letter_num = occupiedFC.getInt("count") % 6;
           char max_letter = 'A';
          if(max_letter_num != 0) max_letter = (char)(max_letter + max_letter_num);
          
          if(max_letter_num == 0) maxRow = maxRow + 1;
          changeQuery.setInt(1, maxRow);
          changeQuery.setString(2, max_letter+" ");
          changeQuery.setString(2, String.valueOf(max_letter));
          
          changeQuery.executeUpdate(); 
  
          firstSeatsAvailable--; 
          pplUpgradedFirstClass++;     
        


        }

      }


        // Removing passangers who we could not upgrade //

        PreparedStatement deleteOverBookedEcon = connection.prepareStatement("DELETE "+
          "FROM ONLY Booking "+
          "WHERE flight_id = ? AND seat_class = 'economy' AND row IS NULL AND letter IS NULL");
        deleteOverBookedEcon.setInt(1,flightID);
        ResultSet exec_deleteOverBookedEcon = deleteOverBookedEcon.executeQuery();


        // Returning Number of Upgrades Made //



       }

       // Case # 3: totalSeats >= numOverbookedPassangers //

       else if (totalSeats >= numOverbookedPassangers) {

        /* Write queries to modify overBookpassangers; when done return numOverbookPassangers */

               while(ppl2Upgrade.next()){

        if(bussinessSeatsAvailable > 0) {

          PreparedStatement changeQuery = connection.prepareStatement("UPDATE Booking "+"SET seat_class = 'business' AND row = ? AND letter = ? " + "WHERE Booking.id = ppl2Upgrade.id");

          int maxRow = occupiedBC.getInt("maxRow");
          int max_letter_num = occupiedBC.getInt("count") % 6;
          char max_letter = 'A';
          if(max_letter_num != 0) max_letter = (char)(max_letter + max_letter_num);
          
          if(max_letter_num == 0) maxRow = maxRow + 1;

          changeQuery.setInt(1, maxRow);
          changeQuery.setString(2, max_letter+" ");
          changeQuery.setString(2, String.valueOf(max_letter));
          

          changeQuery.executeUpdate(); 
  
                  bussinessSeatsAvailable--; 
                  pplUpgradedBussiness++;     
        
        }




        else if(firstSeatsAvailable > 0){

          PreparedStatement changeQuery = connection.prepareStatement("UPDATE Booking " + "SET seat_class = 'first' AND row = ? AND letter = ? " + "WHERE Booking.id = ppl2Upgrade.id ");

          int maxRow = occupiedFC.getInt("maxRow");
          int max_letter_num = occupiedFC.getInt("count") % 6;
          char max_letter = 'A';
          if(max_letter_num != 0) max_letter = (char)(max_letter + max_letter_num);
  
          if(max_letter_num == 0) maxRow = maxRow + 1;
          changeQuery.setInt(1, maxRow);
          changeQuery.setString(2, max_letter+" ");
          changeQuery.setString(2, String.valueOf(max_letter));
          

          changeQuery.executeUpdate(); 
  
                  firstSeatsAvailable--; 
                  pplUpgradedFirstClass++;     
        


                                        }

       }


    }


      }

}
    catch(Exception e){
         e.printStackTrace();
         return -1;
      }
      
      return pplUpgradedFirstClass + pplUpgradedBussiness;
}


   /* ----------------------- Helper functions below  ------------------------- */

    // A helpful function for adding a timestamp to new bookings.
    // Example of setting a timestamp in a PreparedStatement:
    // ps.setTimestamp(1, getCurrentTimeStamp());

    /**
    * Returns a SQL Timestamp object of the current time.
    * 
    * @return           Timestamp of current time.
    */
   private java.sql.Timestamp getCurrentTimeStamp() {
      java.util.Date now = new java.util.Date();
      return new java.sql.Timestamp(now.getTime());
   }

   // Add more helper functions below if desired.


  
  /* ----------------------- Main method below  ------------------------- */

   public static void main(String[] args) {
      // You can put testing code in here. It will not affect our autotester.
      System.out.println("Running the code!");
   }

}
