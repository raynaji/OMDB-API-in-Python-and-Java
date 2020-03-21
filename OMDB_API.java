package ; //under your package.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class OMDB_API {

	// define database connection constants.
	static String url = "jdbc:mysql://localhost:3306/";
	static String username = "root";
	static String password = ""; //your password 
	
	
	// define database:
	static final String SQL_DB = "OMDB";
	// define omdb_test table:
    static final String SQL_TABLE_omdb_test = "omdb_test";
    static final String SQL_TABLE_omdb_test_DEF = "("
            +  "imdb_id VARCHAR(10) NOT NULL PRIMARY KEY"
            + ",title VARCHAR(50)"
            + ",year YEAR"
            + ")";
    // define omdb table:
    static final String SQL_TABLE_omdb = "omdb";
    static final String SQL_TABLE_omdb_DEF = "("
            +  "imdbID VARCHAR(10) NOT NULL PRIMARY KEY"
            + ",title VARCHAR(50)"
            //+ "title VARCHAR(50)"
            + ",year YEAR"
            + ",genre VARCHAR(100)"
            + ",director VARCHAR(50)"
            + ",imdb_rating INT"
            + ",rotten_tomatoes INT"
            + ",metacritic INT"
            + ",plot VARCHAR(300)"
            + ",box_office VARCHAR(50)"
            + ")"; 
    
    static final String api_key = ""; //your api key to OMDB
    
    // Part 1: 
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
    // (a)
        // My API key is "...". 
        // The URL that searches for movies containing the word "blade": 
    		// http://www.omdbapi.com/?s=blade&apikey=ce0abdca
        // If use the parameter 'PLOT' and 'RETURN': 
    		// http://www.omdbapi.com/?s=blade&apikey=...&r=json&plot=short   	
        String omdb_json = urlToString("http://www.omdbapi.com/?s=blade&apikey="+api_key+"&r=json&plot=short" , "Mozilla/5.0");
    
    // (b) 
        // Pretty print the returned JSON.
        JsonParser parser = new JsonParser();
        JsonElement omdb_je = parser.parse(omdb_json);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("======Part 1 (b), Pretty print: ======");
        System.out.println(gson.toJson(omdb_je));
        System.out.println("======================================");
        System.out.println("\n\n");
        
        
    // (c) 
        // Iterate through the "search" results. Print the imdbID to the screen.
        JsonArray omdb_search_array = omdb_je.getAsJsonObject().get("Search").getAsJsonArray();
        System.out.println("======Part 1 (c), Print imdbID: ======");
        
        for (JsonElement el : omdb_search_array) {
        	String imdbID = el.getAsJsonObject().get("imdbID").getAsString();
        	System.out.println(imdbID);
        }
        
    	System.out.println("======================================");
    	System.out.println("\n\n");
        
        
    // Part 2: 
    // (d) 
        // Connect to local SQL. Create the empty omdb_test table.
    	System.out.println("======= Part 2, Create omdb_test: =======");
        createSqlTable(SQL_TABLE_omdb_test, SQL_TABLE_omdb_test_DEF);
        System.out.println("======================================");
    	System.out.println("\n\n");
    	
    // Part 3:
    // (a) Search for and Print the top 10 imbdID and titles.
        // define the top 10 movies;
        String[] top10_title = new String[10];
        top10_title[0] = "Ocean's Eleven";
        top10_title[1] = "Dangal";
        top10_title[2] = "Frozen";
        top10_title[3] = "Identity";
        top10_title[4] = "The Matrix";
        top10_title[5] = "Rush Hour 2";
        top10_title[6] = "Titanic";
        top10_title[7] = "Transformers";
        top10_title[8] = "Joker";
        top10_title[9] = "Pride & Prejudice";
        
        // encode the string to URL
        String[] top10_title_url = new String[10];
        for (int encode = 0; encode < 10; encode ++) {
        	top10_title_url[encode] = URLEncoder.encode(top10_title[encode], StandardCharsets.UTF_8.toString());
        }
        
        // store the imdbID
        String[] top10_imbdID = new String[10];
        
        // Iterate through the top10 movies.
        System.out.println("====== Part 3 (a), Print top10 titles and matched imdbID: ======");
        for (int i=0; i<10; i++) {
        	String term_url = top10_title_url[i];
        	String term = top10_title[i];
        	
        	// connect, get and parse Json.
        	String top_json = urlToString("http://www.omdbapi.com/?s="+term_url+"&apikey=ce0abdca&r=json&plot=short" , "Mozilla/5.0");
            JsonElement top_je = parser.parse(top_json);
            
            // Iterate through the "search" results. Identify the matched movie and print the imdbID to the screen.
            JsonArray top_search_array = top_je.getAsJsonObject().get("Search").getAsJsonArray();
            
            for (int match = 0; match < top_search_array.size(); match++) {
            	String title = top_search_array.get(match).getAsJsonObject().get("Title").getAsString();
            	
            	// compare two strings cannot use ==. string1 == string2 will return the difference of the lengths of the two strings.
            	if (title.contentEquals(term)) {
            		String imdbID = top_search_array.get(match).getAsJsonObject().get("imdbID").getAsString();
            		top10_imbdID[i] = imdbID;
                	System.out.println("Title: " + title +", imdbID: " + imdbID);
                	break;
            	}
            	else {continue;}
            }
			            /* because we don't want to get all the information in search results, so we don't use el:array.
			            for (JsonElement el : top_search_array) {
			            	String title = el.getAsJsonObject().get("Title").getAsString();
			            	// This will return all the titles, but not the first title.
			            	System.out.println(title);
			            	// even if we get the first title, string1 == string2 will return 0 if they have the same length.
			            	if (title == term) {
			            		String imdbID = el.getAsJsonObject().get("imdbID").getAsString();
			            		top10_imbdID[i] = imdbID;
			                	System.out.println(title +" " + imdbID);
			                	break;
			            	}
			            }
			            */
        }
        System.out.println("==========================================================");
        System.out.println("\n\n");
        
    // (b) Search for top10 movies using imbdID. Print the result.
        String[] top_id_json = new String[10];
        
        System.out.println("======== Part 3 (b), Use imdbID to search and print the results: ========");
        
        for (int i = 0; i < 10; i++) {
        	 String term_id = top10_imbdID[i];
        	 // connect, get and parse Json.
        	 top_id_json[i] = urlToString("http://www.omdbapi.com/?i="+term_id+"&apikey=ce0abdca&r=json&plot=short" , "Mozilla/5.0");
             JsonElement top_id_je = parser.parse(top_id_json[i]);
             
             System.out.println(gson.toJson(top_id_je));
             System.out.println("\n");
        }
        
        System.out.println("=======================================================================");
        System.out.println("\n\n");
        
    // (c) Store information into omdb table.
        // Create new table omdb.
        System.out.println("=========== Part3 (c) Create omdb table: ============================");
        createSqlTable(SQL_TABLE_omdb, SQL_TABLE_omdb_DEF);
        System.out.println("=======================================================================");
        System.out.println("\n\n");
        
        // Store information of the top 10 movies.
        try(
    		Connection conn = DriverManager.getConnection(url + SQL_DB, username, password);
    		PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + SQL_TABLE_omdb +  " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        ){
        	for (int i = 0; i < 10; i++) {
        		JsonElement top_id_je_2 = parser.parse(top_id_json[i]);              
        		
                // get information.
                int year = top_id_je_2.getAsJsonObject().get("Year").getAsInt();
                String genre = top_id_je_2.getAsJsonObject().get("Genre").getAsString();
                String director = top_id_je_2.getAsJsonObject().get("Director").getAsString();
                
                // get ratings
                JsonArray ratings = top_id_je_2.getAsJsonObject().get("Ratings").getAsJsonArray();
                
                String imdb = ratings.get(0).getAsJsonObject().get("Value").getAsString().substring(0, 3);
                	//int imdb_rating = Integer.parseInt(imbd)*10;
                int imdb_rating = (int) (Float.parseFloat(imdb)*10);
                
                String tomato = ratings.get(1).getAsJsonObject().get("Value").getAsString().substring(0, 2); 
                int rotten_tomato = Integer.parseInt(tomato);
                
                String metascore = top_id_je_2.getAsJsonObject().get("Metascore").getAsString();
                Integer metacritic = 0; // if there is no metacritic, we give 0.
                if(!metascore.contentEquals("N/A")) {
                	 metacritic = Integer.parseInt(metascore);
                }
                
                String plot = top_id_je_2.getAsJsonObject().get("Plot").getAsString();
                String box_office = top_id_je_2.getAsJsonObject().get("BoxOffice").getAsString(); 
                
                // store information into omdb table.
                stmt.setString(1, top10_imbdID[i]);
                stmt.setString(2, top10_title[i]);
                stmt.setInt(3, year);
                stmt.setString(4, genre);
                stmt.setString(5, director);
                stmt.setInt(6, imdb_rating);
                stmt.setInt(7, rotten_tomato);
                stmt.setInt(8, metacritic);
                stmt.setString(9, plot);
                stmt.setString(10, box_office);
                stmt.addBatch();
                
                System.out.println(i+". row inserted");
                
            }
        	stmt.executeBatch();
        }

    }
    
    
// Defined functions.
    public static int createSqlTable(String tbl, String def) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url, username, password);
        
        Statement stmt = conn.createStatement();

        String query = "CREATE DATABASE IF NOT EXISTS " + SQL_DB;
        stmt.executeUpdate(query);
        System.out.println(SQL_DB+" Database Connected");
        
        
        query = "CREATE TABLE IF NOT EXISTS " + SQL_DB + "." + tbl + " " + def + ";";
        int tb_status = stmt.executeUpdate(query);
        System.out.println(tbl+" Table Created");
        
        System.out.println("\n");
        return tb_status;
        
    }
    
    public static String urlToString(String url, String userAgent) throws IOException {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        connection.setRequestProperty("User-Agent", userAgent);
        
        try (
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
        ) {
            StringBuilder sb = new StringBuilder();
            String inputLine;
            
            while ( (inputLine = in.readLine()) != null ) 
                sb.append(inputLine);
            
            return sb.toString();
        }
    }

}