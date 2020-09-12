#include <iostream>
#include <vector>
#include <map>
#define BEGIN 256
#define CAPACITY (16384-256)
using namespace std;
// read stdin
vector<string> read_stdin() {
    string codeword;
    vector<string> output;
    char s, e;
    while ((s = cin.get()) != '\n') {
        codeword.push_back(s);
        e = cin.get();  //e get the next char of s
        while(e != '\n' && e != ' ') {
            codeword.push_back(e);
            e = cin.get();
        }
        output.push_back(codeword);
        codeword.clear();
        if (e == '\n') {
            break;
        }
    }
    return output;
}
string decode(const vector<string> &input) {
    int index = BEGIN;  // start from 256
    string entry;
    string k;
    string w;
    string output;  //decoded file
    map<unsigned int, string> dict; //  16384 entry
    for(vector<string>::const_iterator c = input.begin(); c != input.end(); c++) {
        k = *c;
        if (c == input.begin()) {
            // w is NIL
            output = output + k;    //output k
            w = k;
        } else {
            if (k.size() == 1) {
                // k is a single ascii code
                output = output + k;    //output dict(k)
                if (dict.size() <= CAPACITY) {    // dict is yet full
                    entry = w+k;
                    dict.insert({index, entry});
                    index++;
                }
                w = k;
            } else if (dict.find(stoi(k)) == dict.end()){
                // kwkwk case
                k = w + w[0];   
                entry = k;
                if (dict.size() <= CAPACITY) {
                    dict.insert({index, entry});
                    index++;
                }
                output += k;
                w = k;
            } else {
                entry = dict.find(stoi(k))->second;
                output += entry;
                if (index-BEGIN <= CAPACITY) {
                    dict.insert({index, w+entry[0]});
                    index++;
                }
                w = entry;
            }
        }
    }
    return output;
}

string decode_with_list(const vector<string> &input) {
    int index = BEGIN;  // start from 256
    string entry;
    string k;
    string w = "NIL";
    string output;  //decoding list
    string line;
    unsigned int prev;  // w in dict's index
    map<unsigned int, string> dict; //  16384 entry
    for(vector<string>::const_iterator c = input.begin(); c != input.end(); c++) {
        k = *c;
        if (c == input.begin()) {
            // k output
            // w is NIL
            line = w+" "+k+" "+k;
            output = line + "\n";
            w = k;
            prev = k[0];
        } else {
            // w k decode(k) index symbol
            if (k.size() == 1) {
                // single ascii
                entry = w + k;
                if (dict.size() < CAPACITY) {
                    if (prev < BEGIN) {
                        line = (char)prev;
                    } else {
                        line = to_string(prev);
                    }
                    line += " "+k+" "+k+" "+to_string(index)+" "+entry;
                    dict.insert({index, entry});
                    index++;
                } else {
                    // w k decode(k);
                    if (prev < BEGIN) {
                        line = (char)prev;
                    } else {
                        line = to_string(prev);
                    }
                    line += " "+k+" "+k;
                }
                output += line + "\n";
                w = k;
                prev = k[0];
            } else if (dict.find(stoi(k)) == dict.end()) {
                // kwkwk
                if (dict.size() < CAPACITY) {
                    // w k decode(k) index symbol 
                    if (prev < BEGIN) {
                        line = (char)prev;
                    } else {
                        line = to_string(prev);
                    }
                    line += " "+k+" ";
                    prev = stoi(k);
                    k = w + w[0];
                    entry = k;
                    line += k+" "+to_string(index)+" "+entry;
                    dict.insert({index, entry});
                    index++;
                } else {
                    if (prev < BEGIN) {
                        line = (char)prev;
                    } else {
                        line = to_string(prev);
                    }
                    line +=" "+k+" ";
                    prev = stoi(k);
                    k = w + w[0];
                    line += k;
                }
                output += line + "\n";
                w = k;
            } else {
                entry = dict.find(stoi(k))->second;
                if (dict.size() < CAPACITY) {
                    if (prev < BEGIN) {
                        line = (char)prev;
                    } else {
                        line = to_string(prev);
                    }
                    line += " "+k+" "+entry+" "+to_string(index)+" "+w+entry[0];
                    dict.insert({index, w+entry[0]});
                    index++;
                } else {
                    if (prev < BEGIN) {
                        line = (char)prev;
                    } else {
                        line = to_string(prev);
                    }
                    line += " "+k+" "+entry;
                }
                output += line +"\n";
                w = entry;
                prev = stoi(k);
            }
        }
    }
    return output;
}
int main(int argc, char* argv[]) {
    string argument;
    vector<string> codewords;
    string error_msg = "Usage: ldecode [-l]";
    string output;
    if (argc == 1) {
        // no paramenter
        codewords = read_stdin();
        output = decode(codewords);
        cout << output << endl;
    } else if (argc == 2) {
        // list mode
        argument = argv[1];
        if (argument != "-l") {
            // error handler for argument
            cerr << error_msg << endl;
        } else {
            codewords = read_stdin();
            output = decode_with_list(codewords);
            cout << output;
        }
    } else {
        // error handler for arguments
        cerr << error_msg << endl;
    }
}