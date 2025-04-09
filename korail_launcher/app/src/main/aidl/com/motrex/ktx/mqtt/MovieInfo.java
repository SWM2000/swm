package com.motrex.ktx.mqtt;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * The {@code MovieInfo} class encapsulates all relevant data related to a movie, 
 * such as its title in Korean and English, genre, playtime, grade (rating), 
 * cast (actors), director, story summary, image URL, and RTSP URL for streaming. 
 * This class implements the {@link Parcelable} interface, which allows 
 * objects of this class to be easily passed between Android components such as 
 * activities and services.
 * 
 * By implementing the {@link Parcelable} interface, the {@code MovieInfo} class 
 * enables the efficient serialization of its instances into {@link Parcel} objects, 
 * making it ideal for Android's inter-process communication (IPC). Parcelable 
 * provides a high-performance alternative to {@link java.io.Serializable}, 
 * avoiding the reflection overhead, which is especially useful in a performance-sensitive 
 * environment like Android.
 */
public class MovieInfo implements Parcelable {

    // Fields representing different attributes of the movie, such as titles, genre, playtime, grade, etc.
    private String titleKor;    // The movie's title in Korean.
    private String titleEng;    // The movie's title in English.
    private String genre;       // The movie's genre (e.g., Action, Drama, etc.).
    private String playTime;    // The movie's runtime or playtime.
    private String grade;       // The movie's rating (e.g., PG, R, etc.).
    private String actors;      // A string representing the main actors in the movie.
    private String director;    // The name of the movie's director.
    private String story;       // A brief description or synopsis of the movie's plot.
    private String imageUrl;    // The URL of the movie's poster or cover image.
    private String rtspUrl;     // The RTSP URL for streaming the movie.

    /**
     * Constructor used for recreating the {@code MovieInfo} object from a {@link Parcel}.
     * This method is part of the {@link Parcelable} implementation and is used when 
     * deserializing an object after it has been written to a {@link Parcel}.
     * 
     * @param in The {@link Parcel} containing the serialized data for a {@code MovieInfo} object.
     */
    protected MovieInfo(Parcel in) {
        // Read and assign the values from the Parcel in the same order they were written.
        titleKor = in.readString();  // Read the Korean title.
        titleEng = in.readString();  // Read the English title.
        genre = in.readString();     // Read the genre.
        playTime = in.readString();  // Read the playtime.
        grade = in.readString();     // Read the movie's grade/rating.
        actors = in.readString();    // Read the actors.
        director = in.readString();  // Read the director's name.
        story = in.readString();     // Read the story description.
        imageUrl = in.readString();  // Read the image URL.
        rtspUrl = in.readString();   // Read the RTSP URL.
    }

    /**
     * Writes the {@code MovieInfo} object's data into a {@link Parcel}, which allows the object 
     * to be serialized and transferred across Android components or processes. 
     * The order of writing fields here must match the order of reading them in the constructor.
     * 
     * This method is called by the Android framework when passing the {@code MovieInfo} object 
     * between components or storing it persistently.
     *
     * @param dest  The {@link Parcel} object where the data will be written.
     * @param flags Additional flags for how the object should be written (not used here).
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write the fields to the Parcel in the same order they will be read.
        dest.writeString(titleKor);  // Write the Korean title.
        dest.writeString(titleEng);  // Write the English title.
        dest.writeString(genre);     // Write the genre.
        dest.writeString(playTime);  // Write the playtime.
        dest.writeString(grade);     // Write the grade/rating.
        dest.writeString(actors);    // Write the actors.
        dest.writeString(director);  // Write the director's name.
        dest.writeString(story);     // Write the story.
        dest.writeString(imageUrl);  // Write the image URL.
        dest.writeString(rtspUrl);   // Write the RTSP URL.
    }

    /**
     * Describes the contents of the {@code MovieInfo} object for Parcelable. 
     * This method returns 0 because no special objects like file descriptors 
     * are being used in this class. It is part of the {@link Parcelable} interface.
     * 
     * @return Always returns 0, as no special objects are included.
     */
    @Override
    public int describeContents() {
        return 0;  // No special objects (e.g., file descriptors) are used in this Parcelable class.
    }

    /**
     * Static field that is used to create new instances of the {@code MovieInfo} class from a 
     * {@link Parcel}. This is a requirement of the {@link Parcelable} interface and allows 
     * the Android system to efficiently re-create instances of this class during the IPC process.
     */
    public static final Creator<MovieInfo> CREATOR = new Creator<MovieInfo>() {

        /**
         * Creates a new {@code MovieInfo} instance from the given {@link Parcel}.
         * This method is used when deserializing the {@code MovieInfo} object from 
         * a parcel after it has been serialized.
         *
         * @param in The {@link Parcel} containing the serialized data.
         * @return A new {@code MovieInfo} object created from the data in the {@link Parcel}.
         */
        @Override
        public MovieInfo createFromParcel(Parcel in) {
            return new MovieInfo(in);  // Re-create the MovieInfo object from the Parcel.
        }

        /**
         * Creates a new array of {@code MovieInfo} objects of the specified size.
         * This method is used when creating arrays of {@link Parcelable} objects.
         *
         * @param size The size of the array to be created.
         * @return An array of {@code MovieInfo} objects.
         */
        @Override
        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];  // Return a new array of MovieInfo objects.
        }
    };

    /**
     * Provides a string representation of the {@code MovieInfo} object, 
     * which includes all its fields such as titles, genre, playtime, 
     * rating, actors, director, story, and URLs. This method is 
     * primarily used for debugging and logging purposes, allowing 
     * developers to easily view the content of a {@code MovieInfo} instance.
     *
     * @return A string representation of the {@code MovieInfo} object.
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

    // Getter and setter methods to access and modify the fields of the MovieInfo class.
    // These methods provide controlled access to the private fields, allowing external 
    // classes to interact with the data encapsulated in the MovieInfo object.
    // Getters allow reading the values of fields, while setters allow updating them.

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