package com.example.notesapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class NotesModel {




    private Integer count;

    private ArrayList<NotesDetails> items;


    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public ArrayList<NotesDetails> getItems() {
        return items;
    }

    public void setItems(ArrayList<NotesDetails> items) {
        this.items = items;
    }

    public class NotesDetails{


        private ArrayList<String> tags;

        private String _id;

        private String content;

        private String author;

        private Integer length;

        public NotesDetails(ArrayList<String> tags, String _id, String content, String author, Integer length) {
            this.tags = tags;
            this._id = _id;
            this.content = content;
            this.author = author;
            this.length = length;
        }

        public ArrayList<String> getTags() {
            return tags;
        }

        public void setTags(ArrayList<String> tags) {
            this.tags = tags;
        }

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

    }

}
