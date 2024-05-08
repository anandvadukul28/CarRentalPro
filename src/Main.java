import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.io.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.net.URLEncoder;

class CarDetails {
    private String carName;
    private String site;
    private double price;
    private double rating;
    private int passengers;

    public CarDetails(String carName, String site, double price, double rating) {
        this.carName = carName;
        this.site = site;
        this.price = price;
        this.rating = rating;
        this.passengers = passengers;
    }

    public String getCarName() {
        return carName;
    }

    public String getSite() {
        return site;
    }

    public double getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public int getPassengers() {
        return passengers;
    }
}

public class Main {
    private static final Set<String> matches = new HashSet<>();
    public static Map<String, Set<String>> invertedIndex = new HashMap<>();
    private static final String INDEX_FILE_PATH = "invertedIndex.txt";
    private static final String SEARCH_FREQUENCY_FILE = "search_frequency.txt";
    static String path = "/Users/anandvadukul/Downloads/FinalProject 2";

    public static void main(String[] args) throws UnsupportedEncodingException {

        Map<String, Integer> searchFrequencyMap = new HashMap<>();


        Scanner scanner = new Scanner(System.in);

        boolean continueLoop = true;

        while (continueLoop) {
            System.out.println("Select an option:");
            System.out.println("1. Web Crawler");
            System.out.println("2. Inverted Indexing");
            System.out.println("3. Frequency Count of each car in the fetched data");
            System.out.println("4. Page Ranking");
            System.out.println("5. Spell Checking");
            System.out.println("6. Word Completion");
            System.out.println("7. Search Frequency");
            System.out.println("8. Finding Patterns Using Regular Expressions");
            System.out.println("9. Find top 10 Cheapest Deals");
            System.out.print("Enter your choice (1, 2, 3, 4, 5, 6, 7, 8 or 9): ");

            if(scanner.hasNextInt()) {
                int userChoice = scanner.nextInt();
                scanner.nextLine();

                switch (userChoice) {
                    case 1:

                        System.setProperty("webdriver.chrome.driver", "/Users/anandvadukul/Downloads/chromedriver-mac-arm64/chromedriver");
                        scanner = new Scanner(System.in);

                        String location = "";
                        while (true) {
                            // Get user input for location
                            while (true) {
                                System.out.print("Enter location: ");
                                location = scanner.nextLine();

                                // Check if the location contains any special characters
                                if (!location.matches("^[a-zA-Z]+$")) {
                                    System.out.println("Special characters and Numbers are not allowed. Please enter a valid location.");
                                } else {
                                    System.out.println("Valid location. Proceeding...");
                                    break;
                                }
                            }
                            // Find nearest word with edit distance from locations.txt
                            List<String> nearestWords = findNearestWords(location);

                            // Ask the user if any of the suggested words is correct
                            if (!nearestWords.isEmpty()) {
                                System.out.println("Did you mean one of the following?");
                                for (String word : nearestWords) {
                                    System.out.println("- " + word);
                                }

                                System.out.print("(Type the correct word or 'no' to enter a new one): ");
                                String userResponse = scanner.nextLine();

                                if (!userResponse.equalsIgnoreCase("no") && nearestWords.contains(userResponse)) {
                                    System.out.println("Location set to: " + userResponse);
                                    location = userResponse;
                                    break; // Exit the loop if the user confirms the location
                                } else {
                                    System.out.println("Please enter the correct spelling of the location.");
                                }
                            } else {
                                System.out.println("No suggestions found. Please enter the correct spelling of the location.");
                            }
                        }

                        String startDate = "";
                        String endDate = "";

                        while (true) {
                            System.out.print("Enter start date (yyyy-MM-dd) for All Urls: ");
                            startDate = scanner.nextLine();

                            try {
                                LocalDate enteredDate = LocalDate.parse(startDate);

                                // Check if the entered date is not in the past
                                if (enteredDate.isBefore(LocalDate.now())) {
                                    System.out.println("Invalid date. Please enter a future date.");
                                } else {
                                    System.out.println("Valid date format. Proceeding...");
                                    break;
                                }
                            } catch (DateTimeParseException e) {
                                System.out.println("Invalid date. Please enter a valid date.");
                            }
                        }
                        while (true) {
                            System.out.print("Enter end date (yyyy-MM-dd) for All Urls: ");
                            endDate = scanner.nextLine();

                            try {
                                LocalDate enteredDate = LocalDate.parse(endDate);//parsing date

                                // Check if the entered date is not in the past
                                if (enteredDate.isBefore(LocalDate.now())) {
                                    System.out.println("Invalid date. Please enter a future date.");
                                } else if (enteredDate.isBefore(LocalDate.parse(startDate)) || enteredDate.isEqual(LocalDate.parse(startDate))) {
                                    System.out.println("End date should be greater than the start date. Please enter a valid end date.");
                                } else {
                                    System.out.println("Valid date format. Proceeding...");
                                    break;
                                }
                            } catch (DateTimeParseException e) {
                                System.out.println("Invalid date. Please enter a valid date.");
                            }
                        }

                        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);

                        // Construct dynamic URLs
                        String carRentalsURL = String.format("https://www.carrentals.com/carsearch?locn=%s&date1=%s&date2=%s&",
                                encodedLocation, startDate, endDate);

                        String expediaURL = String.format("https://www.expedia.com/carsearch?locn=%s&date1=%s&date2=%s",
                                encodedLocation, startDate, endDate);

                        String orbitzURL = String.format("https://www.orbitz.com/carsearch?locn=%s&date1=%s&date2=%s",
                                encodedLocation, startDate, endDate);


                        WebDriver driver = new ChromeDriver();

                        deleteTextFiles(path);
                        try {
                            // Example 1: carrentals.com
                            driver.get(carRentalsURL);
                            Thread.sleep(4000);
                            extractCarDetailsCarRentals(driver);

                            // Example 2: Expedia.com
                            driver.get(expediaURL);
                            Thread.sleep(4000);
                            extractCarDetailsExpedia(driver);
//             Example 3: Hotwire.com

                            driver.get(orbitzURL);
                            Thread.sleep(4000);
                            extractCarDetailsOrbitz(driver);


                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            driver.quit();
                            deleteInvertedIndexFile();
                        }
                        break;
                    case 2:
                        //Index search
                        // Step 1: Check if the Inverted Index File Exists
                        if (indexFileExists()) {
                            // Load the Inverted Index from the file
                            loadInvertedIndex();
                        } else {
                            // Step 2: Create an Inverted Index
                            indexFiles(new File(path));
                            // Step 3: Store the Inverted Index
                            storeInvertedIndex();
                        }
                        // Step 4: Search through the Inverted Index
                        Scanner sc = new Scanner(System.in);
                        System.out.print("Enter the word with which you want to do Inverted Indexing: ");
                        String str = sc.nextLine();
                        search(str);
                        break;
                    case 3:

                        // Creating HashMap for storing word frequency
                        Map<String, Integer> wordFrequencyMap = new HashMap<>();

                        //path of folder that contains text files
                        String folderPath = path;

                        // Get all files in the folder
                        File folder = new File(folderPath);
                        File[] files = folder.listFiles();

                        // Check if the folder is not empty
                        if (files != null) {
                            // Iterate through each file
                            for (File file : files) {
                                if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                                    // Process the file and update word frequency map
                                    processFile(file.getAbsolutePath(), wordFrequencyMap);
                                }
                            }
                        } else {
                            System.out.println("Folder is empty or does not exist.");
                        }

                        // Display the word frequency
                        displayWordFrequency(wordFrequencyMap);
                        break;
                    case 4:
                        //page ranking Algo
                        List<CarDetails> carDetailsList = (List<CarDetails>) readCarDetailsFromFiles(path);
                        scanner = new Scanner(System.in);
                        List<String> queryKeywords = new ArrayList<>();

                        // Ask the user for keywords until they decide to stop
                        while (true) {
                            System.out.println("Enter a search keyword:");
                            String keyword = scanner.nextLine();
                            queryKeywords.add(keyword);
                            // Update the search frequency for the entered keyword
                            updateSearchFrequency(searchFrequencyMap, keyword);
                            // Update the file with the current search frequency
                            writeSearchFrequencyToFile(searchFrequencyMap);
                            // Ask the user if they want to search again
                            System.out.println("Do you want to add new keyword? (yes/no)");
                            String response = scanner.nextLine();
                            if (response.equalsIgnoreCase("no")) {
                                break;
                            }
                        }

                        // Rank and display search results
                        List<CarDetails> rankedCars = rankCars(carDetailsList, queryKeywords);
                        displaySearchResults(rankedCars, queryKeywords);
                        break;
                    case 5:

                        List<String> vocabulary = constructVocabulary(path);

                        // Spell Checking Example
                        scanner = new Scanner(System.in);
                        System.out.print("Enter the word you want to know the correct spelling.");
                        String wordToCheck = scanner.nextLine();
                        String correctedWord = spellCheck(wordToCheck, vocabulary);
                        System.out.println("Original Word: " + wordToCheck);
                        System.out.println("Corrected Word: " + correctedWord);
                        break;
                    case 6:
                        Trie trie = new Trie();
                        List<String> vocabularyOfCarNames = constructVocabularyOfCarNames(path);
                        for (String carName : vocabularyOfCarNames) {
                            trie.insert(carName);
                        }
                        scanner = new Scanner(System.in);
                        System.out.print("Enter the car name you want to complete: ");
                        String partialWord = scanner.nextLine();
                        List<String> completions = wordCompletion(partialWord, trie);
                        System.out.println("Partial Word: " + partialWord);
                        System.out.println("Word Completions: " + completions);
                        break;
                    case 7:

                        displaySearchFrequency(searchFrequencyMap, readSearchFrequencyFromFile());
                        break;
                    case 8:
                        //Finding Patterns Using Regular Expressions
                        scanner = new Scanner(System.in);
                        System.out.print("Enter a regular expression to search: ");
                        String regex = scanner.nextLine();
                        // Search for the pattern in the crawled data
                        searchPatterns(regex);
                        break;
                    case 9:
                        List<String> providers = List.of("CarRentals", "Expedia", "Orbitz"); // Add more providers if needed

                        List<CarDeal> allDeals = new ArrayList<>();

                        // Process each provider
                        for (String provider : providers) {
                            List<CarDeal> dealsFromFile = readAndProcessFiles(provider);
                            allDeals.addAll(dealsFromFile);
                        }

                        // Find the top 10 deals with the cheapest prices
                        List<CarDeal> top10Deals = findTopNDeals(allDeals, 10);

                        // Print the top 10 deals
                        System.out.println("Top 10 Car Deals:");
                        top10Deals.forEach(System.out::println);

                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1, 2, 3, 4, 5, 6, 7 or 8.");
                }
            }
            else
            {
                System.out.println("Invalid Input. Please enter a valid number.");
                scanner.nextLine();
            }
            // Ask the user if they want to continue
            System.out.print("Do you want to continue? (yes/no): ");
            String userResponse = scanner.next().toLowerCase();

            // Check if the user wants to continue
            continueLoop = userResponse.equals("yes");
        }

        // Close the scanner to prevent resource leak
        scanner.close();
    }
    private static List<CarDeal> readAndProcessFiles(String provider) {
        List<CarDeal> deals = new ArrayList<>();
        int fileNumber = 1;

        while (true) {
            String filename = String.format("%s%d.txt", provider, fileNumber);
            File file = new File(filename);

            if (!file.exists()) {
                break; // No more files for this provider
            }

            List<CarDeal> dealsFromFile = readCarDealsFromFile(file);
            deals.addAll(dealsFromFile);

            fileNumber++;
        }

        return deals;
    }

    private static List<CarDeal> readCarDealsFromFile(File file) {
        List<CarDeal> deals = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String carName = null;
            String carType = null;
            double price = 0.0;
            String carSite = null; // Default value if "Available at" is not found in the file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    if ("Car Name".equals(key)) {
                        carName = value;
                    } else if ("Car Type".equals(key)) {
                        carType = value;
                    } else if ("Car Total Price".equals(key)) {
                        try {
                            // Remove non-numeric characters from the value, e.g., "$"
                            value = value.replaceAll("[^0-9.]", "");
                            price = Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            e.printStackTrace(); // Handle the exception based on your requirements
                        }
                    } else if ("Available at".equals(key)) {
                        carSite = value;
                    }
                } else if (line.trim().isEmpty()) {
                    // Empty line indicates the end of a deal
                    if (carName != null && carType != null) {
                        deals.add(new CarDeal(carName, carType, price, carSite));
                        carName = null;
                        carType = null;
                        price = 0.0;
                        carSite = null; // Reset the values for the next deal
                    }
                }
            }
            // Add the last deal if any
            if (carName != null && carType != null) {
                deals.add(new CarDeal(carName, carType, price, carSite));
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception based on your requirements
        }

        return deals;
    }


    private static List<CarDeal> findTopNDeals(List<CarDeal> deals, int n) {
        // Sort the deals by price in ascending order
        deals.sort(Comparator.comparingDouble(CarDeal::getPrice));

        // Return the top N deals (cheapest)
        return deals.subList(0, Math.min(n, deals.size()));
    }
    private static void deleteTextFiles(String directoryPath) {
        File directory = new File(directoryPath);

        // Check if the specified path is a directory
        if (directory.isDirectory()) {
            // Get a list of files in the directory
            File[] files = directory.listFiles();

            // Check if there are any files
            if (files != null && files.length > 0) {
                // Iterate through the files and delete text files
                for (File file : files) {
                    if (file.isFile() && !file.getName().equals("invertedIndex.txt") && !file.getName().equals("locations.txt") && !file.getName().equals("search_frequency.txt") && file.getName().toLowerCase().endsWith(".txt")) {
                        if (file.delete()) {
                            System.out.println("Deleted: " + file.getAbsolutePath());
                        } else {
                            System.out.println("Failed to delete: " + file.getAbsolutePath());
                        }
                    }
                }
            } else {
                System.out.println("The directory is empty.");
            }
        } else {
            System.out.println("Invalid directory path.");
        }
    }

    private static void searchPatterns(String regex) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            for (String filePath : getFilesInDirectory(path)) {
                executor.submit(() -> processRegFile(filePath, regex));
            }
        } finally {
            executor.shutdown();
            try {
                // Wait for all threads to finish
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private static void processRegFile(String filePath, String regex) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);

                try {
                    if (matcher.find()) {
                        String matchInfo = "Match found in file: " + filePath + "\n" + line;
                        synchronized (matches) {
                            if (matches.add(matchInfo)) {
                                System.out.println(matchInfo);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing line in file: " + filePath);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String[] getFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        // Check if the specified path is a directory
        if (directory.isDirectory()) {
            // Get a list of files in the directory
            File[] files = directory.listFiles();

            // Check if there are any files
            if (files != null && files.length > 0) {
                // Create a list to store file paths
                List<String> filePathsList = new ArrayList<>();

                // Extract file paths from File objects, excluding invertedIndex.txt
                for (File file : files) {
                    // Check if it's a file and not invertedIndex.txt before adding to the list
                    if (file.isFile() && !file.getName().equals("invertedIndex.txt") && !file.getName().equals("locations.txt") && !file.getName().equals("search_frequency.txt")) {
                        filePathsList.add(file.getAbsolutePath());
                    }
                }

                // Convert the list to an array
                String[] filePaths = filePathsList.toArray(new String[0]);
                return filePaths;
            } else {
                System.out.println("The directory is empty.");
            }
        } else {
            System.out.println("Invalid directory path.");
        }

        // Return an empty array if there's an issue
        return new String[0];
    }

    private static void updateSearchFrequency(Map<String, Integer> searchFrequencyMap, String keyword) {
        // Update the search frequency for the entered keyword
        searchFrequencyMap.put(keyword, searchFrequencyMap.getOrDefault(keyword, 0) + 1);
    }

    private static void displaySearchFrequency(Map<String, Integer> updatedSearchFrequency, Map<String, Integer> currentSearchFrequency) {
        System.out.println("Current Search Frequency:");
        for (Map.Entry<String, Integer> entry : currentSearchFrequency.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " times");
        }
    }


    private static void writeSearchFrequencyToFile(Map<String, Integer> searchFrequencyMap) {
        Map<String, Integer> existingData = readSearchFrequencyFromFile();

        // Update existingData with new data
        for (Map.Entry<String, Integer> entry : searchFrequencyMap.entrySet()) {
            String keyword = entry.getKey();
            int frequency = entry.getValue() + existingData.getOrDefault(keyword, 0);
            existingData.put(keyword, frequency);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SEARCH_FREQUENCY_FILE))) {
            // Write the updated search frequency to the file
            for (Map.Entry<String, Integer> entry : existingData.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Integer> readSearchFrequencyFromFile() {
        Map<String, Integer> existingData = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(SEARCH_FREQUENCY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    String keyword = parts[0];
                    int frequency = Integer.parseInt(parts[1]);
                    existingData.put(keyword, frequency);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return existingData;
    }


    private static List<CarDetails> readCarDetailsFromFiles(String directoryPath) {
        List<CarDetails> carDetailsList = new ArrayList<>();

        File directory = new File(directoryPath);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && !file.getName().equals("invertedIndex.txt") && !file.getName().equals("locations.txt") && !file.getName().equals("search_frequency.txt") && file.getName().endsWith(".txt")) {
                        CarDetails carDetails = readCarDetailsFromFile(new File(file.getAbsolutePath()));
                        if (carDetails != null) {
                            carDetailsList.add(carDetails);
                        }
                    }
                }
            }
        } else {
            System.out.println("Invalid directory path: " + directoryPath);
        }

        return carDetailsList;
    }


    private static CarDetails readCarDetailsFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String carNameLine = reader.readLine(); // Assuming the first line contains "Car Name:"
            String carTypeLine = reader.readLine();
            String passengersLine = reader.readLine();
            String pricePerDayLine = reader.readLine();
            // Skip the line "Car Total Price:" as it's not needed
            reader.readLine();
            String vendorNameLine = reader.readLine();
            String ratingsLine = reader.readLine();

            String carName = extractValue(carNameLine, "Car Name:");
            String carType = extractValue(carTypeLine, "Car Type:");
            int passengers = extractIntValue(passengersLine, "Max Passengers:");
            double pricePerDay = extractDoubleValue(pricePerDayLine, "Car Price per Day:");
            String vendorName = extractValue(vendorNameLine, "Vendor Name:");

            double ratings = 0.0; // Default value if ratings line is not provided
            if (ratingsLine != null) {
                ratings = extractDoubleValue(ratingsLine);
            }

            return new CarDetails(carName, carType, passengers, pricePerDay);
        } catch (IOException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error reading car details from file: " + file.getName());
            //e.printStackTrace();
        }

        return null;
    }

    // Helper method to extract the value after a specified string
    private static String extractValue(String input, String start) {
        int startIndex = input.indexOf(start) + start.length();
        if (startIndex != -1) {
            return input.substring(startIndex).trim();
        }
        return "";
    }

    // Helper method to extract integer value after a specified string
    private static int extractIntValue(String input, String start) {
        String value = extractValue(input, start);
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    // Helper method to extract double value after a specified string
    private static double extractDoubleValue(String input, String start) {
        String value = extractValue(input, start);
        return value.isEmpty() ? 0.0 : Double.parseDouble(value.replace("$", ""));
    }
//
//    private static String extractValue(String line) {
//        if (line != null && line.contains(":")) {
//            return line.split(":")[1].trim();
//        }
//        return "";
//    }

    private static double extractDoubleValue(String line) {
        if (line != null && line.contains(":")) {
            String value = line.split(":")[1].trim().replace("$", "");

            // Handle "N/A" case
            if (value.equalsIgnoreCase("N/A")) {
                return 0.0; // or any other default value you prefer
            }

            return value.isEmpty() ? 0.0 : Double.parseDouble(value);
        }
        return 0.0;
    }


    private static List<CarDetails> rankCars(List<CarDetails> cars, List<String> queryKeywords) {
        // Calculate a relevance score for each car based on keyword occurrences
        Map<CarDetails, Integer> carScores = new HashMap<>();

        for (CarDetails car : cars) {
            int score = calculateRelevanceScore(car, queryKeywords);
            carScores.put(car, score);
        }

        // Sort cars based on relevance score in descending order
        List<CarDetails> rankedCars = new ArrayList<>(carScores.keySet());
        rankedCars.sort(Comparator.comparingInt(carScores::get).reversed());

        return rankedCars;
    }

    private static int calculateRelevanceScore(CarDetails car, List<String> queryKeywords) {
        // Calculate the relevance score based on the frequency of query keywords in car details
        int score = 0;

        for (String keyword : queryKeywords) {
            if (car.getCarName().contains(keyword) || car.getSite().contains(keyword)) {
                score++;
            }
        }

        return score;
    }

    private static void displaySearchResults(List<CarDetails> rankedCars, List<String> queryKeywords) {
        System.out.println("Search Results:");
        for (int i = 0; i < rankedCars.size(); i++) {
            CarDetails car = rankedCars.get(i);
            System.out.println((i + 1) + ". Car Name: " + car.getCarName() + ", CarType: " + car.getSite() + ", Relevance Score: " + calculateRelevanceScore(car, queryKeywords));
        }
    }

    private static List<String> constructVocabularyOfCarNames(String directoryPath) {
        List<String> vocabulary = new ArrayList<>();

        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".txt")) {
                        String carName = readCarNamesFromFile(file);
                        if (!carName.isEmpty()) {
                            vocabulary.addAll(List.of(carName.split(" ")));
                        }
                    }
                }
            }
        } else {
            System.err.println("Invalid directory path: " + directoryPath);
        }

        return vocabulary;
    }

    private static String readCarNamesFromFile(File filePath) {
        String carName = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Read each line from the file
            while ((line = reader.readLine()) != null) {
                // Check if the line contains "Car Name:"
                if (line.contains("Car Name:")) {
                    // Extract the car name from the line
                    String carNameLine = extractValue(line, "Car Name:").trim();
                    carName = carNameLine.split(" or ")[0].toLowerCase(); // Extracting the car name before " or "
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return carName;
    }

    // Construct vocabulary based on existing words in text files
    private static List<String> constructVocabulary(String directoryPath) {
        List<String> vocabulary = new ArrayList<>();

        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && !file.getName().equals("invertedIndex.txt") && !file.getName().equals("locations.txt") && !file.getName().equals("search_frequency.txt") && file.getName().endsWith(".txt")) {
                        vocabulary.addAll(readWordsFromFile(file));
                    }
                }
            }
        } else {
            System.err.println("Invalid directory path: " + directoryPath);
        }

        return vocabulary;
    }

    private static List<String> readWordsFromFile(File filePath) {
        List<String> wordsList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read each line from the file
            while ((line = reader.readLine()) != null) {
                // Split the line into words
                String[] words = line.split("\\s+");

                // Add each word to the list
                for (String word : words) {
                    wordsList.add(word);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wordsList;

    }

    // Spell Check using Minimum Edit Distance
    private static String spellCheck(String word, List<String> vocabulary) {
        int minDistance = Integer.MAX_VALUE;
        String correctedWord = word;

        for (String validWord : vocabulary) {
            int distance = calculateEditDistance(word, validWord);
            if (distance < minDistance) {
                minDistance = distance;
                correctedWord = validWord;
            }
        }

        return correctedWord;
    }

    private static List<String> wordCompletion(String partialWord, Trie trie) {
        List<String> completions = new ArrayList<>();

        TrieNode node = trie.searchNode(partialWord);
        if (node != null) {
            // If the partial word is found in the Trie, perform a depth-first search to gather completions
            gatherCompletions(node, completions, partialWord);
        }

        return completions;
    }

    // Helper method for depth-first search to gather completions
    private static void gatherCompletions(TrieNode node, List<String> completions, String currentPrefix) {
        // Check if the current node is the end of a word
        if (node.isEndOfWord()) {
            completions.add(currentPrefix);
        }

        // Recursively gather completions for each child node
        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            char childChar = entry.getKey();
            TrieNode childNode = entry.getValue();
            gatherCompletions(childNode, completions, currentPrefix + childChar);
        }
    }
    // Calculate Minimum Edit Distance between two words
    private static int calculateEditDistance(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();

        int[][] dp = new int[m + 1][n + 1];

        // Build the DP table
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }

        return dp[m][n];
    }


    private static void saveToFile(String fileName, String content) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidURL(String url) {
        // Define a regular expression for a simple URL validation
        String urlRegex = "^(https?|ftp)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?$";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(urlRegex);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(url);

        // Check if the URL matches the pattern
        return matcher.matches();
    }

    private static void indexFiles(File directory) {
        // Recursively index all text files in the given directory
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if(!file.getName().equals("locations.txt")) {
                        indexFiles(file);
                    }
                }
            }
        } else if (directory.isFile() && directory.getName().endsWith(".txt")) {
            // Index the content of each text file
            indexFile(directory);
        }
    }
    private static void indexFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Tokenize the content into words
                String[] words = line.split("\\s+");
                // Update the inverted index
                for (String word : words) {
                    invertedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(file.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(String fileName, Map<String, Integer> wordFrequencyMap) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            // Read each line from the file
            while ((line = reader.readLine()) != null) {
                // Check if the line contains "Car Name:"
                if (line.contains("Car Name:")) {
                    // Extract the car name from the line
                    String carNameLine = extractTextAfter(line, "Car Name:").trim();
                    String carName = carNameLine.split(" or ")[0].toLowerCase(); // Extracting the car name before " or "

                    // Update word frequency in the map
                    wordFrequencyMap.put(carName, wordFrequencyMap.getOrDefault(carName, 0) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to extract text after a specified string
    private static String extractTextAfter(String input, String start) {
        int startIndex = input.indexOf(start) + start.length();
        if (startIndex != -1) {
            return input.substring(startIndex);
        }
        return "";
    }


    private static void displayWordFrequency(Map<String, Integer> wordFrequencyMap) {
        // Create a max heap using a PriorityQueue and a custom comparator
        PriorityQueue<WordFrequency> mH = new PriorityQueue<>(
                Comparator.comparingInt(WordFrequency::getFrequency).reversed()
        );

        // Add all entries to the max heap
        wordFrequencyMap.forEach((word, frequency) -> mH.add(new WordFrequency(word, frequency)));

        // Display the sorted word frequency
        while (!mH.isEmpty()) {
            WordFrequency wordFrequency = mH.poll();
            System.out.println(wordFrequency.word + ": " + wordFrequency.frequency);
        }
    }

    private static void search(String query) {
        // Tokenize the search query
        String[] queryTerms = query.split("\\s+");

        // Search through the inverted index
        Set<String> resultFiles = new HashSet<>();
        for (String term : queryTerms) {
            Set<String> termFiles = invertedIndex.getOrDefault(term, new HashSet<>());
            if (resultFiles.isEmpty()) {
                resultFiles.addAll(termFiles);
            } else {
                resultFiles.retainAll(termFiles);
            }
        }

        // Display the search results
        System.out.println("Search Results:");
        for (String file : resultFiles) {
            System.out.println("- " + file);
        }
    }

    private static void storeInvertedIndex() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(INDEX_FILE_PATH))) {
            outputStream.writeObject(invertedIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadInvertedIndex() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(INDEX_FILE_PATH))) {
            invertedIndex = (Map<String, Set<String>>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean indexFileExists() {
        File indexFile = new File(INDEX_FILE_PATH);
        return indexFile.exists() && indexFile.isFile();
    }
    private static void extractCarDetailsCarRentals(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {

            // Wait for the car result items to be present
            List<WebElement> offerCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("li.offer-card-desktop")));
            int count = 0;
            if (offerCards.isEmpty()) {
                System.out.println("No Cars Available for this day.");
            } else {
                for (int i = 0; i < offerCards.size(); i++) {
                    WebElement offerCard = offerCards.get(i);
                    WebElement carName;
                    try {
                        carName = offerCard.findElement(By.className("uitk-text"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carName = null;
                    }
                    String cName = (carName != null) ? carName.getText().trim() : "No/A";
                    WebElement carType;
                    try {
                        carType = offerCard.findElement(By.className("uitk-heading-5"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carType = null;
                    }
                    String cType = (carType != null) ? carType.getText() : "N/A";
                    WebElement perDayPrice;
                    try {
                        perDayPrice = offerCard.findElement(By.cssSelector("span.per-day-price"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        perDayPrice = null;
                    }
                    String pperday = (perDayPrice != null) ? perDayPrice.getText().trim() : "N/A";
                    WebElement totalPrice;
                    try {
                        totalPrice = offerCard.findElement(By.cssSelector("span.total-price"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        totalPrice = null;
                    }
                    String tprice = (totalPrice != null) ? totalPrice.getText().trim() : "N/A";
                    WebElement vendorName;
                    try {
                        vendorName = offerCard.findElement(By.cssSelector("img.vendor-logo"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        vendorName = null;
                    }
                    String vendor = (vendorName != null) ? vendorName.getAttribute("alt") : "N/A";
                    WebElement carPass;
                    try {
                        carPass = offerCard.findElement(By.className("text-attribute"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carPass = null;
                    }
                    String pass = (carPass != null) ? carPass.getText() : "N/A";
                    WebElement carRat;
                    try {
                        carRat = offerCard.findElement(By.className("uitk-text-white-space-break-spaces"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carRat = null;
                    }
                    String carSite = "carrentals";
                    String ratings = (carRat != null) ? carRat.getText() : "No Ratings";
                    System.out.println("-------CarRental Deals--------");
                    System.out.println("Car Name: " + cName);
                    System.out.println("Car Type: " + cType);
                    System.out.println("Passengers: " + pass);
                    System.out.println("Price per day: " + pperday);
                    System.out.println("Car Total Price: " + tprice);
                    System.out.println("Vendor Name: " + vendor);
                    System.out.println("Ratings: " + ratings);
                    System.out.println("Available at: " + carSite);
                    System.out.println("------------------------------=");
                    count = count + 1;
                    saveToFile("CarRentals" + count + ".txt", "Car Name: " + cName + "\nCar Type: " + cType + "\nMax Passengers: " + pass + "\nCar Price per Day: " + pperday + "\nCar Total Price: " + tprice + "\nVendor Name: " + vendor + "\nRatings " + ratings +"\nAvailable at: "+carSite);

                }
            }
        }catch (TimeoutException e){
            System.out.println("No cars available at carrentals.com for this city.");
        }
    }

    private static void extractCarDetailsExpedia(WebDriver driver) {
        // Find all car result items within the container
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            List<WebElement> carDetailElements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("li.offer-card-desktop")));
            int count = 0;
            if (carDetailElements.isEmpty()) {
                System.out.println("No Cars Available for this day.");
            } else {
                // Extract information for each car detail
                for (WebElement carDetailElement : carDetailElements) {
                    WebElement carNameElement;
                    try {
                        carNameElement = carDetailElement.findElement(By.className("uitk-text"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carNameElement = null;
                    }
                    String carName = (carNameElement != null) ? carNameElement.getText() : "N/A";

                    WebElement carTypeElement;
                    try {
                        carTypeElement = carDetailElement.findElement(By.className("uitk-heading"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carTypeElement = null;
                    }
                    String carType = (carTypeElement != null) ? carTypeElement.getText() : "N/A";

                    WebElement carPassElement;
                    try {
                        carPassElement = carDetailElement.findElement(By.className("text-attribute"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carPassElement = null;
                    }
                    String pass = (carPassElement != null) ? carPassElement.getText() : "N/A";
                    WebElement carpperdayElement;
                    try {
                        carpperdayElement = carDetailElement.findElement(By.className("per-day-price"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carpperdayElement = null;
                    }
                    String pperday = (carpperdayElement != null) ? carpperdayElement.getText() : "N/A";

                    WebElement carPriceElement;
                    try {
                        carPriceElement = carDetailElement.findElement(By.className("total-price"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carPriceElement = null;
                    }
                    String carCost = (carPriceElement != null) ? carPriceElement.getText() : "N/A";

                    WebElement carVendorElement;
                    try {
                        carVendorElement = carDetailElement.findElement(By.className("vendor-logo"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carVendorElement = null;
                    }
                    String vendor = (carVendorElement != null) ? carVendorElement.getAttribute("alt") : "N/A";

                    WebElement carRatElement;
                    try {
                        carRatElement = carDetailElement.findElement(By.className("uitk-text-white-space-break-spaces"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carRatElement = null;
                    }
                    String ratings = (carRatElement != null) ? carRatElement.getText() : "No Ratings";
                    String carSite = "expedia";

                    System.out.println("--------------Expedia Deals-----------------");
                    System.out.println("Car Name: " + carName);
                    System.out.println("Car Type: " + carType);
                    System.out.println("Passengers: " + pass);
                    System.out.println("Price per day: " + pperday);
                    System.out.println("Car Total Price: " + carCost);
                    System.out.println("Vendor Name:" + vendor);
                    System.out.println("Ratings: " + ratings);
                    System.out.println("Available at: " + carSite);
                    System.out.println("----------------------------------");
                    count = count + 1;
                    saveToFile("Expedia" + count + ".txt", "Car Name: " + carName + "\nCar Type: " + carType + "\nMax Passengers: " + pass + "\nCar Price per Day: " + pperday + "\nCar Total Price: " + carCost + "\nVendor Name: " + vendor + "\nRatings " + ratings +"\nAvailable at: "+carSite);

                }
            }
        }catch (TimeoutException e){
            System.out.println("No cars Available at Expedia.com for this city");
        }
    }
    private static void extractCarDetailsOrbitz(WebDriver driver) {
        int count = 0;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try{
            List<WebElement> carDetailElements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("li.offer-card-desktop")));

            if (carDetailElements.isEmpty()) {
                System.out.println("No Cars Available for this day.");
            } else {
                // Extract information for each car detail
                for (WebElement carDetailElement : carDetailElements) {
                    WebElement carNameElement;
                    try {
                        carNameElement = carDetailElement.findElement(By.className("uitk-text"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carNameElement = null;
                    }
                    String carName = (carNameElement != null) ? carNameElement.getText() : "N/A";

                    WebElement carTypeElement;
                    try {
                        carTypeElement = carDetailElement.findElement(By.className("uitk-heading"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carTypeElement = null;
                    }
                    String carType = (carTypeElement != null) ? carTypeElement.getText() : "N/A";

                    WebElement carPassElement;
                    try {
                        carPassElement = carDetailElement.findElement(By.className("text-attribute"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carPassElement = null;
                    }
                    String pass = (carPassElement != null) ? carPassElement.getText() : "N/A";
                    WebElement carpperdayElement;
                    try {
                        carpperdayElement = carDetailElement.findElement(By.className("per-day-price"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carpperdayElement = null;
                    }
                    String pperday = (carpperdayElement != null) ? carpperdayElement.getText() : "N/A";

                    WebElement carPriceElement;
                    try {
                        carPriceElement = carDetailElement.findElement(By.className("total-price"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carPriceElement = null;
                    }
                    String carCost = (carPriceElement != null) ? carPriceElement.getText() : "N/A";

                    WebElement carVendorElement;
                    try {
                        carVendorElement = carDetailElement.findElement(By.className("vendor-logo"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carVendorElement = null;
                    }
                    String vendor = (carVendorElement != null) ? carVendorElement.getAttribute("alt") : "N/A";

                    WebElement carRatElement;
                    try {
                        carRatElement = carDetailElement.findElement(By.className("uitk-text-white-space-break-spaces"));
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        carRatElement = null;
                    }
                    String ratings = (carRatElement != null) ? carRatElement.getText() : "No Ratings";
                    String carSite = "orbitz";
                    System.out.println("--------------Orbitz Deals--------------------");
                    System.out.println("Car Name: " + carName);
                    System.out.println("Car Type: " + carType);
                    System.out.println("Passengers: " + pass);
                    System.out.println("Price per day: " + pperday);
                    System.out.println("Car Total Price: " + carCost);
                    System.out.println("Vendor Name:" + vendor);
                    System.out.println("Ratings: " + ratings);
                    System.out.println("Available at:" +carSite);
                    System.out.println("----------------------------------");
                    count = count + 1;
                    saveToFile("Orbitz" + count + ".txt", "Car Name: " + carName + "\nCar Type: " + carType + "\nMax Passengers: " + pass + "\nCar Price per Day: " + pperday + "\nCar Total Price: " + carCost + "\nVendor Name: " + vendor + "\nRatings " + ratings +"\nAvailable at: "+carSite);

                }
            }
        }
        catch (TimeoutException e){
            System.out.println("No cars Available at Orbitz.com for this city");
        }
    }
    private static List<String> findNearestWords(String input) {
        List<String> nearestWords = new ArrayList<>();
        int minDistance = Integer.MAX_VALUE;

        try (BufferedReader reader = new BufferedReader(new FileReader("locations.txt"))) {
            String line;

            while ((line = reader.readLine()) != null) {
                int distance = calculateEditDistance(input, line.trim());

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestWords.clear(); // Clear previous words since a new minimum is found
                    if (!nearestWords.contains(line.trim())) {
                        // If it doesn't contain the word, then add it
                        nearestWords.add(line.trim());
                    }
                } else if (distance == minDistance) {
                    if (!nearestWords.contains(line.trim())) {
                        // If it doesn't contain the word, then add it
                        nearestWords.add(line.trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nearestWords;
    }
    private static void deleteInvertedIndexFile() {
        // Specify the file path
        String filePath = "invertedIndex.txt";

        // Use Paths.get() to create a Path object
        Path path = Paths.get(filePath);

        // Check if the file exists before attempting to delete
        if (Files.exists(path)) {
            try {
                // Use Files.delete() to delete the file
                Files.delete(path);
                System.out.println("File deleted successfully.");
            } catch (IOException e) {
                // Handle the exception if the file deletion fails
                System.out.println("Unable to delete the file: " + e.getMessage());
            }
        } else {
            System.out.println("File does not exist. No deletion needed.");
        }
    }
    private static class WordFrequency {
        String word;
        int frequency;

        WordFrequency(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }

        int getFrequency() {
            return frequency;
        }
    }


}
class CarDeal {
    private static int dealNo = 0;
    private String carName;
    private String carType;
    private double price;// Assuming price is a double
    private String carSite;
    private int dealno;

    public CarDeal(String carName, String carType, double price, String carSite) {
        this.carName = carName;
        this.carType = carType;
        this.price = price;
        this.carSite = carSite;
        this.dealno = ++dealNo;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.format("%d. Car Name: %s%n   Car Type: %s%n   Price: $%.2f%n   Available at: %s%n",
                dealno, carName, carType, price, carSite);
    }
}
class TrieNode {
    private Map<Character, TrieNode> children;
    private boolean isEndOfWord;
    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
    }
    public Map<Character, TrieNode> getChildren() {
        return children;
    }
    public boolean isEndOfWord() {
        return isEndOfWord;
    }
    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }
}
class Trie {
    private TrieNode root;
    public Trie() {
        this.root = new TrieNode();
    }
    public void insert(String word) {
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            node.getChildren().computeIfAbsent(ch, c -> new TrieNode());
            node = node.getChildren().get(ch);
        }
        node.setEndOfWord(true);
    }
    public TrieNode searchNode(String prefix) {
        TrieNode node = root;
        for (char ch : prefix.toCharArray()) {
            if (!node.getChildren().containsKey(ch)) {
                return null;
            }
            node = node.getChildren().get(ch);
        }
        return node;
    }
}