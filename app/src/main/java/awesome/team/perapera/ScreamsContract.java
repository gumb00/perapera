package awesome.team.perapera;

import android.provider.BaseColumns;

/**
 * This class contains all columns that are used in the SQLite tables. Created by gumb on 25.01.15.
 */
public final class
        ScreamsContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ScreamsContract() {

    }

    /* Inner class that defines the table contents */
    public static abstract class PostEntry implements BaseColumns {
        // The posts table to store all posts
        public static final String POSTS_TABLE_NAME = "posts";
        public static final String KEY_ID = "id";
        public static final String KEY_TOPIC_ID = "topic_id";
    }

    /* Inner class that defines the posts table to store all topics*/
    public static abstract class TopicEntry implements BaseColumns {
        public static final String TOPICS_TABLE_NAME = "topics";
        public static final String KEY_TITLE = "title";
        public static final String KEY_ID = "id";
    }
}