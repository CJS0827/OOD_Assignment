package service;

import model.User;
import java.io.*;
import java.util.ArrayList;

public class ManageCustomerService {

    private final String file = "data/users.txt";

    public ArrayList<User> loadAllUsers() {

        ArrayList<User> allUsers = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;

            while((line = br.readLine()) != null) {

                String[] data = line.split("\\|");

                User u = new User(
                        data[0],
                        data[1],
                        data[2],
                        data[3],
                        data[4],
                        data[5],
                        data[6],
                        data[7],
                        data[8]
                );

                allUsers.add(u);
            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        return allUsers;
    }



    public ArrayList<User> getCustomers(ArrayList<User> allUsers) {

        ArrayList<User> customers = new ArrayList<>();

        for(User u : allUsers) {

            if(u.getRole().equalsIgnoreCase("Customer")) {

                customers.add(u);

            }

        }

        return customers;
    }



    public void saveAllUsers(ArrayList<User> allUsers) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

            for(User u : allUsers) {

                String line =
                        u.getId() + "|" +
                        u.getUsername() + "|" +
                        u.getPassword() + "|" +
                        u.getPhone() + "|" +
                        u.getEmail() + "|" +
                        u.getSecurityQuestion() + "|" +
                        u.getSecurityAnswer() + "|" +
                        u.getRole() + "|" +
                        u.getStatus();

                bw.write(line);
                bw.newLine();
            }

        } catch(IOException e) {

            e.printStackTrace();

        }
    }



    public String getNextCustomerId(ArrayList<User> allUsers) {

        int max = 0;

        for(User u : allUsers) {

            if(u.getRole().equalsIgnoreCase("Customer")) {

                try {

                    int num = Integer.parseInt(u.getId().substring(1));

                    if(num > max)
                        max = num;

                } catch(Exception e) {}

            }

        }

        return String.format("U%03d", max + 1);
    }

}