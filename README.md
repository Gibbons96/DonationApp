# Donation Tracker App

A simple Android app built with Jetpack Compose to track charitable donations.

## Features Implemented

1. **Splash Screen** - Welcome screen with app branding
2. **Login Screen** - Simple authentication screen (demo mode)
3. **Donation List** - View all donations in a list format
4. **Add Donation** - Form to add new donations with:
   - Title
   - Description
   - Amount
   - Category selection
   - Date (auto-generated)
5. **Delete Donations** - Remove donations from the list
6. **Map View** - Placeholder for Google Maps integration
7. **Location Support** - Ready for location tracking (permissions added)

## Screens

- **SplashScreen**: App introduction with loading animation
- **LoginScreen**: Authentication options (Google Sign-In placeholder)
- **DonationListScreen**: Main screen showing all donations
- **AddDonationScreen**: Form to add new donations
- **DonationMapScreen**: Map view placeholder for donation locations

## Technical Details

- Built with **Jetpack Compose**
- Uses **Material 3** design system
- Simple state management with `remember` and `mutableStateOf`
- No complex architecture - suitable for assignment projects
- Sample data included for demonstration

## Future Enhancements

- Google Sign-In integration
- Google Maps integration
- Local database storage
- Cloud synchronization
- Location tracking for donations
- Image upload support

## How to Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Run on an emulator or physical device

The app will start with a splash screen, then show the login screen, and finally the main donation list. 