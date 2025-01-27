# MapApp

MapApp is an Android application that provides location-based services such as searching for places, finding routes, reverse geocoding, and viewing current location using Google Maps. This app leverages Android SDK, Google Maps API, and Places API for its core functionalities.

---

## Features

1. **Current Location Display**  
   - Shows the user's current location on the map with a marker.  
   - Automatically moves the camera to the current location.

2. **Place Search Autocomplete**  
   - Provides search suggestions for places using Google Places API.  
   - Fetches place details, including latitude and longitude.  

3. **Route Calculation**  
   - Displays a route between the user's current location and a selected destination.  
   - Adds a polyline on the map to represent the route.  

4. **Reverse Geocoding**  
   - Converts latitude and longitude into a human-readable address by long-pressing on the map.  
   - Displays the address with a marker.  

5. **Map Modes**  
   - Switch between different map modes (Normal, Terrain, Hybrid) using a segment control.  

6. **User-friendly Interface**  
   - Interactive buttons for finding current location and calculating routes.  
   - Search bar for finding and selecting locations easily.  

---

## Prerequisites

To run this application, you need the following:

1. Android Studio (latest version recommended).  
2. A valid Google API Key with the following APIs enabled:  
   - Maps SDK for Android  
   - Places API  
   - Directions API  
   - Geocoding API  

3. Add the API Key to the `strings.xml` file:  
   ```xml
   <string name="API_KEY">YOUR_API_KEY</string>
   ```

---

## Setup and Installation

1. Clone the repository or download the source code.  
2. Open the project in Android Studio.  
3. Sync the project with Gradle.  
4. Add your API Key to `res/values/strings.xml`.  
5. Run the app on an emulator or a physical device with location permissions enabled.

---

## How to Use

1. **Current Location**:  
   - Tap the "Current Location" button to move the camera to your current location.  

2. **Search for Places**:  
   - Type in the search bar to get place suggestions.  
   - Select a place to set it as the destination.  

3. **Calculate Route**:  
   - After selecting a destination, tap the "Route" button to display the route between your current location and the destination.  

4. **Reverse Geocoding**:  
   - Long press on the map to get the address of the selected location.  

5. **Map Modes**:  
   - Use the segment control to switch between Normal, Terrain, and Hybrid map modes.

---

## Permissions

The app requires the following permissions:  
1. **Location Permission**  
   - Used to access the user's current location.  

Ensure the app has been granted location permissions in the device settings.

---

## Known Issues

1. Location retrieval might fail if GPS is disabled.  
2. Errors may occur if API Key is missing or invalid.  
3. Route calculation requires both current location and destination to be available.

---

## Libraries and Dependencies

1. **Google Maps SDK**  
   - Used for displaying and interacting with the map.  

2. **Google Places API**  
   - Used for search autocomplete and fetching place details.  

3. **Google Directions API**  
   - Used for route calculation between locations.  

4. **Kotlin Coroutines**  
   - Used for asynchronous programming and API calls.  

5. **AndroidX Libraries**  
   - Lifecycle, ViewBinding, and Core libraries.  

---

## Future Enhancements

1. Add real-time traffic information.  
2. Include support for multiple waypoints on routes.  
3. Improve error handling and user notifications.  
4. Implement offline map functionality.  

---

## License

This project is for educational purposes and does not have a license for commercial use.  

---

### Author

**Keshav Agarwal**
An Android development enthusiast learning the basics of Kotlin and building practical projects to enhance skills.  

Feel free to reach out for suggestions or collaboration! 
