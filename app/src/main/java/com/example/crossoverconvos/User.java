package com.example.crossoverconvos;

/**
 * Represents a user with personal and security details.
 * This class stores information such as the user's name, email, date of birth,
 * favorite team, and security question and answer.
 */
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String dateOfBirth;
    private String favoriteTeam;
    private String securityQuestion;
    private String securityAnswer;

    /**
     * Default constructor for creating an instance of User with no initial values.
     */
    public User() {
    }

    /**
     * Constructs a new User with specified details.
     *
     * @param firstName        the first name of the user
     * @param lastName         the last name of the user
     * @param email            the email address of the user
     * @param dateOfBirth      the date of birth of the user
     * @param favoriteTeam     the favorite team of the user
     * @param securityQuestion the security question for account recovery
     * @param securityAnswer   the answer to the security question
     */
    public User(String firstName, String lastName, String email, String dateOfBirth, String favoriteTeam, String securityQuestion, String securityAnswer) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.favoriteTeam = favoriteTeam;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }
    /**
     * Returns the first name of the user.
     * @return the first name of the user
     */
    public String getFirstName() {
        return firstName;
    }
    /**
     * Sets the first name of the user.
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    /**
     * Returns the last name of the user.
     * @return the last name of the user
     */
    public String getLastName() {
        return lastName;
    }
    /**
     * Sets the last name of the user.
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the email address of the user.
     * @return the email address of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the date of birth of the user.
     * @return the date of birth of the user
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the date of birth of the user.
     * @param dateOfBirth the date of birth to set
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Returns the favorite team of the user.
     * @return the favorite team of the user
     */
    public String getFavoriteTeam() {
        return favoriteTeam;
    }

    /**
     * Sets the favorite team of the user.
     * @param favoriteTeam the favorite team to set
     */
    public void setFavoriteTeam(String favoriteTeam) {
        this.favoriteTeam = favoriteTeam;
    }

    /**
     * Returns the security question of the user.
     * @return the security question of the user
     */
    public String getSecurityQuestion() {
        return securityQuestion;
    }

    /**
     * Sets the security question of the user.
     * @param securityQuestion the security question to set
     */
    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    /**
     * Returns the security answer of the user.
     * @return the security answer of the user
     */
    public String getSecurityAnswer() {
        return securityAnswer;
    }

    /**
     * Sets the security answer of the user.
     * @param securityAnswer the security answer to set
     */
    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

}
