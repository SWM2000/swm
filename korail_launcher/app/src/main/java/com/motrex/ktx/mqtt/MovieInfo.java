package com.motrex.ktx.mqtt;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * The {@code MovieInfo} class represents the detailed information about a movie, 
 * including titles in different languages, genre, playtime, rating (grade), 
 * actors, director, story synopsis, and various URLs related to the movie. 
 * This class is designed to be easily passed between Android components using the 
 * {@link Parcelable} interface, allowing it to be serialized and deserialized into a 
 * {@link Parcel} and transferred between activities, services, and other Android components.
 * 
 * By implementing Parcelable, the {@code MovieInfo} object can be used as part of 
 * Android's inter-process communication (IPC) mechanism, enabling the transfer of 
 * complex data structures between components without incurring the overhead of 
 * reflection, which is common when using {@link java.io.Serializable}. The Parcelable 
 * interface allows for fast and efficient serialization and deserialization, which is 
 * critical in a performance-sensitive environment like Android.
 */
public class MovieInfo implements Parcelable {

    // Private fields storing various attributes of the movie.
    // These include the movie's title in Korean and English, its genre, playtime, grade, actors,
    // director, a brief story description, and URLs for the poster image and the RTSP streaming URL.

    private String titleKor;    // The title of the movie in Korean.
    private String titleEng;    // The title of the movie in English.
    private String genre;       // The genre of the movie (e.g., Action, Comedy, Drama, etc.).
    private String playTime;    // The total duration or playtime of the movie (e.g., 120 minutes).
    private String grade;       // The rating or grade of the movie (e.g., PG-13, R, etc.).
    private String actors;      // A string listing the actors in the movie, often separated by commas.
    private String director;    // The director of the movie.
    private String story;       // A brief synopsis or description of the movie's storyline.
    private String imageUrl;    // The URL pointing to an image (e.g., a poster) associated with the movie.
    private String rtspUrl;     // The RTSP (Real-Time Streaming Protocol) URL used for streaming the movie.

    /**
     * Constructs a {@code MovieInfo} object from a {@link Parcel}. 
     * This constructor is used by the Android system to create an instance of the 
     * {@code MovieInfo} class from the serialized data stored in the {@code Parcel}.
     * 
     * Parcels are used to store a flattened representation of an object, including all 
     * its fields, in a format that can be transferred across process boundaries or 
     * between activities and services. The constructor reads the values back from 
     * the parcel, effectively "rehydrating" the object.
     *
     * @param in The {@link Parcel} containing the serialized data for the {@code MovieInfo} object.
     */
    protected MovieInfo(Parcel in) {
        // Read and assign the values from the Parcel to the corresponding fields.
        // The order of these reads must match the order of the writes in the writeToParcel method.
        titleKor = in.readString();  // Read the Korean title from the parcel.
        titleEng = in.readString();  // Read the English title from the parcel.
        genre = in.readString();     // Read the genre from the parcel.
        playTime = in.readString();  // Read the playtime from the parcel.
        grade = in.readString();     // Read the grade (rating) from the parcel.
        actors = in.readString();    // Read the actors from the parcel.
        director = in.readString();  // Read the director's name from the parcel.
        story = in.readString();     // Read the story description from the parcel.
        imageUrl = in.readString();  // Read the image URL from the parcel.
        rtspUrl = in.readString();   // Read the RTSP URL from the parcel.
    }

    /**
     * Writes the {@code MovieInfo} object's data to a {@link Parcel}, allowing the object 
     * to be serialized and transferred between Android components. The order in which the 
     * fields are written to the parcel must exactly match the order in which they are read 
     * back in the constructor {@link #MovieInfo(Parcel)}.
     *
     * This method is called when the system needs to serialize the {@code MovieInfo} object 
     * to send it across processes or store it persistently. Efficient serialization is critical 
     * for performance, especially in scenarios like IPC or saving the state of an application.
     *
     * @param dest  The {@link Parcel} where the object's data will be written.
     * @param flags Additional flags for controlling how the object is written (not used here).
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write each field to the Parcel in the same order they were read in the constructor.
        dest.writeString(titleKor);  // Write the Korean title to the parcel.
        dest.writeString(titleEng);  // Write the English title to the parcel.
        dest.writeString(genre);     // Write the genre to the parcel.
        dest.writeString(playTime);  // Write the playtime to the parcel.
        dest.writeString(grade);     // Write the grade (rating) to the parcel.
        dest.writeString(actors);    // Write the actors to the parcel.
        dest.writeString(director);  // Write the director's name to the parcel.
        dest.writeString(story);     // Write the story description to the parcel.
        dest.writeString(imageUrl);  // Write the image URL to the parcel.
        dest.writeString(rtspUrl);   // Write the RTSP URL to the parcel.
    }

    /**
     * Describes the contents of the {@code MovieInfo} object, specifically for Parcelable. 
     * This method is primarily used when the Parcelable object contains special objects such as file descriptors, 
     * which is not the case here. As a result, this method always returns 0.
     *
     * @return Always returns 0, indicating no special objects.
     */
    @Override
    public int describeContents() {
        return 0;  // Indicating that the object has no special file descriptors or objects.
    }

    /**
     * Static field used to regenerate the {@code MovieInfo} object from a {@link Parcel}. 
     * This field is required by the {@link Parcelable} interface and is automatically called 
     * by the Android framework when a {@code MovieInfo} object needs to be recreated from a parcel.
     */
    public static final Creator<MovieInfo> CREATOR = new Creator<MovieInfo>() {
        /**
         * Creates a new instance of {@code MovieInfo} from the given {@link Parcel}. 
         * This method is called when the system needs to rehydrate the {@code MovieInfo} 
         * object from its serialized state stored in the parcel.
         *
         * @param in The {@link Parcel} containing the serialized data.
         * @return A new instance of {@code MovieInfo} with the data read from the parcel.
         */
        @Override
        public MovieInfo createFromParcel(Parcel in) {
            return new MovieInfo(in);  // Create a new MovieInfo object from the parcel data.
        }

        /**
         * Creates a new array of {@code MovieInfo} objects of the specified size.
         * This method is used when the system needs to create an array of Parcelable objects.
         *
         * @param size The size of the array to be created.
         * @return A new array of {@code MovieInfo} objects.
         */
        @Override
        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];  // Create a new array of MovieInfo objects.
        }
    };

    /**
     * Provides a string representation of the {@code MovieInfo} object, which includes 
     * all the movie's attributes such as titles, genre, playtime, and URLs. This method 
     * is useful for debugging or logging purposes, as it provides a quick and easy way 
     * to see all the relevant details about the movie.
     *
     * The {@code toString()} method is often overridden in Java classes to provide more 
     * meaningful output when the object is printed or logged, rather than just printing 
     * the object's memory address.
     *
     * @return A string representation of the movie's details, including its titles, genre, playtime, grade, actors, director, story, image URL, and RTSP URL.
     */
    @NonNull
    @Override
    public String toString() {
        return "Movie{" +
                "titleKor='" + titleKor + '\'' +
                ", titleEng='" + titleEng + '\'' +
                ", genre='" + genre + '\'' +
                ", playTime='" + playTime + '\'' +
                ", grade='" + grade + '\'' +
                ", actors='" + actors + '\'' +
                ", director='" + director + '\'' +
                ", story='" + story + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", rtspUrl='" + rtspUrl + '\'' +
                '}';
    }

    // Getters and Setters for all the fields, allowing external code to access or modify the movie details.
    // These methods are essential for encapsulation, ensuring that the internal representation of the object
    // is only modified through controlled mechanisms, while still allowing access to its properties.

    public String getTitleKor() {
        return titleKor;
    }

    public void setTitleKor(String titleKor) {
        this.titleKor = titleKor;
    }

    public String getTitleEng() {
        return titleEng;
    }

    public void setTitleEng(String titleEng) {
        this.titleEng = titleEng;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRtspUrl() {
        return rtspUrl;
    }

    public void setRtspUrl(String rtspUrl) {
        this.rtspUrl = rtspUrl;
    }
}