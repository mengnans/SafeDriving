package com.example.safedriving;

/**
 * Created by Dan on 23/09/2017.
 */

public class TestClass {
}

//TODO easy way to list userdataitems

    String[] toyNames = ToyBox.getToyNames();

        /*
         * Iterate through the array and append the Strings to the TextView. The reason why we add
         * the "\n\n\n" after the String is to give visual separation between each String in the
         * TextView. Later, we'll learn about a better way to display lists of data.
         */
        for (String toyName : toyNames) {
                mToysListTextView.append(toyName + "\n\n\n");
                }
