# Password-Dataset-Organizer

## Project Description

This repository contains a project for organizing, indexing, and searching a dataset of passwords. It includes features for downloading password files, structuring them into directories, processing for indexing, and performing efficient searches.

## Project Phases

### Preparation Phase
1. **Downloading Data from GitHub:**
   - Download password files from Daniel Miessler's SecLists repository, specifically from the "Common-Credentials" folder. These files will form the main dataset of the project.
   - [SecLists Repository](https://github.com/danielmiessler/SecLists/tree/master/Passwords/Common-Credentials)

2. **Creating Project Folder Structure:**
   - Create a folder in your project to store the downloaded password files and name it "Unprocessed-Passwords".
   - Create another folder named "Processed" to move processed password files into.
   - Create a folder named "Code" to store scripts and code files you will use in the project.
   - Create a folder named "Index" for storing the results of indexing passwords. This folder will contain subfolders organized alphabetically (e.g., 'a', 'b', '0', '@').

### Indexing Process
3. **Reading Files and Sorting by First Character:**
   - Open each file in the "Unprocessed-Passwords" folder individually and read each password.
   - Determine which "Index" subfolder the password should be saved in based on its first character. Create this subfolder if it does not exist.

4. **Saving Passwords:**
   - Save each password in the format "Password|MD5Hash|Sha128|Sha256|source_file_name". This format includes the password itself, its MD5, SHA-128, and SHA-256 hash values, and the name of the source file.
   - When saving passwords to files, create a new file in the same folder if the current one exceeds 10,000 passwords.

5. **Case Sensitivity and Preventing Duplicates:**
   - Consider case sensitivity when indexing passwords and prevent the same password from being recorded multiple times.

### Search Function
6. **Password Search:**
   - Receive a password query from the user and search for this password in the "Index" folder. If the password is found, provide the relevant information; otherwise, save the searched password in the format "Password|MD5Hash|Sha128|Sha256|search". The searched password will be found in the next search since it has been added to the list.

7. **Measuring Search Performance:**
   - Measure the search times for 10 randomly chosen passwords and calculate the average of these times.

### Updates and Maintenance
8. **Adding New Files:**
   - When new files are added to the "Unprocessed-Passwords" folder, process these files as well, update the indexes in the "Index" folder, and integrate them into the existing structure.
