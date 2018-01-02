[Written on 12/28/2017]

This file will serve as a guide for someone who wants to understand how the OutpostLite program is
structured. It should be the starting place for people looking to make changes to the program.

It is NOT the place to look for install or usage instructions.


OutpostLite was originally written by Jon Force.
If you're from the future and not working for the company anymore (and you're having trouble with
the codebase) you can contact me here : vanaficionado@gmail.com

The github repository lives at https://github.com/JonForce/OutpostLite




                                    The PcToTablet Package
----------------------------------------------------------------------------------------------------
The data that is transferred from the PC to the tablet is appropriately referred to as the PcToTablet
package. The package is a zip file that is transferred by the desktop application to the Download
folder on the tablet.


Inside the zip file there is the following :

- All of the images. Their naming scheme is detailed elsewhere.
- image_data.txt  (this file is not used currently)
- Parcel_Data.xml (this contains all of the data about all of the parcels)


This file is unzipped and loaded into the applications internal storage in the Import Activity.

Details on the importing process :
- Importing delete's EVERYTHING in internal storage.
- Parses the parcel data and puts some of it into the ParcelData table in the Sqlite database.
    Only a few very basic pieces of data are put into the database. The most important thing that's
    in each row is the XML_LOCATION. This is the index of where the parcel's data is in the
    parcel_data xml file. The idea here is that you can search the database by print key or address
    and then if you want more information on the parcel the program will use the XML_LOCATION to
    get the rest of the parcel info from the parcel_data xml file.

After all the data is imported, the data lives in internal storage and the download folder is not
accessed until the export process.

Think of the Download folder as the bridge between the PC and Tablet.





                                        How Searching Works
----------------------------------------------------------------------------------------------------
The search terms are input in the MainActivity view. When the search button is pressed, the search
terms are pulled from the view and a Search Activity intent is created. The search terms are passed
as "extras" on the intent. The SearchActivity is the actual process that does the search.

The Search works in the following way :
- All of the ParcelData is parsed into memory. It's unsorted and stored in HashMaps / ArrayLists.
- A Search object is created with the search terms that were input in the main activity. The Search
    will launch and generate a query that will search the ParcelDataTable. The results are returned
    back to the SearchActivity.
- The search results are displayed on the scrolling view using an adapter.
- When a search result is pressed, we do the following :
       - Get the associated XML_LOCATION of the selected result.
       - Pull the Parcel from the Parcels at XML_LOCATION
       - Launch a ViewParcel activity with the Parcel's Data attached as an extra.





                                      How Parcel Data is Viewed
----------------------------------------------------------------------------------------------------
The actual viewing of a parcel happens mostly in the ViewParcel Activity. The ParcelData is passed
to ViewParcel from the search.

In order to get the ParcelData on the screen, there exists a superclass ParcelDataActivity that
will take all of the ParcelData from the intent and find all of the corresponding views on the screen
and fill them in.

Basically, the idea is that if there is a view on the screen with a name matching something in
ParcelData, that view will automatically be set to the text of the data in ParcelData.

Example :

If there is a view with the ID "SBL", it will automatically be filled in with the SBL of the parcel.

This saves us a lot of code because we don't manually have do to findViewById("SBL").setText(...)
for every single piece of data.

The reason that this occurs in a superclass and not directly in ViewParcel is because there are a
few activities that need to have data filled in from the ParcelData. (Such as the improvements page)






                               But Jon, Where's the Code that Handles The Images?
----------------------------------------------------------------------------------------------------
There are more than one activities that need to display the parcel's images. For this reason, all
the image related code exists in a class, ParcelImageActivity. It's an extension of ParcelDataActivity.

The class handles the following :
- Filling the scrollview with all of the images associated with the parcel.
- Launching the image capture service when the "add image" button is pressed.
- Allowing users to set images as default.
- Allowing users to delete an image.
- Showing / Hiding the popup window that shows the full sized image when a thumbnail is pressed.

[NOTE : The ParcelImageActivity works very closely with the ModFile. More on this in the next section]

However, you can't just extend the ParcelImageActivity and it work. If you're creating a new activity
that needs to have the image functionality, you need to have all of the views that ParcelImageActivity
depends on.

These include (but are not limited to) :
"zoomedImage"
"closeButton"
"makeDefaultButton"
"deleteButton"

Also, your new image rich activity needs to have one of the following attached as extras :
- ParcelData
- Three String extras, "SWIS", "PRINT_KEY", and "PARCEL_ID".

The ParcelImageActivity needs the swis, print key and parcel ID so that it can properly work with
images.






                 How are changes (such as image deletions) communicated to the PC?
----------------------------------------------------------------------------------------------------
When a change that affects RPS happens, two things always must happen :
- The change should occur on the tablet.
- The change should be recorded in the ModFile.

The ModFile is a file that logs all of the changes that have happened on the tablet. When the PC
receives the ModFile it will make all those changes in RPS.

There are many types of changes that occur on the tablet that need to follow this pattern.
Some examples :
- Image is deleted.
- Image is set to be the default.
- New Image is added.
- Total assessment value changed.

To better illustrate this concept, this is how an image delete occurs on the tablet.
- First, the Image is deleted from the tablet.
- Next, we record in the ModFile that we deleted a specific image.

The way that we modify the ModFile is always through ModFileAccess. This ensures there is only one
point of contact between the program and the ModFile. It also contains all the concrete documentation
about what the ModFile looks like.

Some closing notes on the ModFile :
- The ModFile is exported to the Download dir during the export process.
- The ModFile may contain redundant changes. For instance, it is valid for the ModFile to contain
    several SET_DEFAULT tags for the same Parcel. This is redundant because only the last image that
    you set to be the default will actually be the default. This could be optimized but it is really
    not a problem right now.







                                    How Does Exporting Work?
----------------------------------------------------------------------------------------------------
The exporting process is actually really simple. All exporting occurs in the ExportActivity.

Basically, all the files that are internal app storage are just zipped together and put into the
Download directory.

Remember, the Download directory is the bridge between the PC and Tablet.

The export package is appropriately named TabletToPC.zip