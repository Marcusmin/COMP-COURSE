#include <iostream>
#include <map>
#include <vector>
#include <cstdio>
#define CAPACITY (16384 - 256)
#define BEGIN 256
using namespace std;
// read stdin
string read_stdin(void) {
    char c;
    long i = 0;
    string res;
    while ((c = cin.get())!='\n') { //input terminated by newline
        res.push_back(c);
        i++;
    }
    return res;
}

vector<unsigned int> encode(const string &input) {
    char k;
    unsigned int index = BEGIN;    // append code of ascii
    string w;   // output string for w, currently NIL
    string symbol;
    vector<unsigned int> output;
    map<string, unsigned int> dict;  //symbol & index
    for(string::const_iterator c = input.begin(); c != input.end(); c++) {
        k = *c; // read the char from input string
        if (w.empty()) {
            // start step, w is NIL and output nothing
            w.push_back(k);
        } else {
            symbol = w + k; // concat wk
            if (dict.find(symbol) == dict.end()) {
                // if wk not exists in the dictionary
                // check if dictionary is full or not
                if (dict.size() < CAPACITY) {
                    dict.insert({symbol, index}); // add wk to dictionary
                    index++;
                }
                // output w
                if (dict.find(w) != dict.end()) {
                    output.push_back(dict.find(w)->second);
                } else {
                    // if w is not in dict which means w should be a single char
                    output.push_back(w[0]);
                }
                w.clear();
                w.push_back(k); // w = k
            } else {
                w.push_back(k);
            }
        }
    }
    // k is EOF, output w only
    if (w.length() > 1) {
        output.push_back(dict.find(w)->second);
    } else {
        output.push_back(w[0]);
    }
    return output; 
}

string encode_with_list(const string &input) {
    char k;  // NIL
    int index = 256;    // append code of ascii
    string w;   // output string for w
    unsigned int code_w; // output code for w
    string symbol;
    string output = "";
    string line;    // line of list
    map<string, unsigned int> dict;  //symbol & index
    for(string::const_iterator c = input.begin(); c != input.end(); c++) {
        k = *c; // read the char from input string
        if (w.empty()) {
            // start step
            w.push_back(k);
            line = "NIL "+w;
            output = line+'\n';
            line.clear();
        } else {
            symbol = w + k; // concat wk
            if (dict.find(symbol) == dict.end()) {
                // if wk not exists in the dictionary
                if (dict.size() < CAPACITY) {
                    dict.insert({symbol, index}); // add wk to dictionary
                    if (dict.find(w) != dict.end()) {   // w is in dict
                        // w k output(code of w) index symbol
                        line = w+" "+k+" "+to_string(dict.find(w)->second)+
                        " "+to_string(index)+" "+symbol;
                    } else {    // w is not in dict
                        // w k ouput index symbol
                        line = w+" "+k+" "+w+
                        " "+to_string(dict.find(symbol)->second)+" "+symbol;                
                    }
                    index++;
                } else {
                    // dict is full, insertion won't occur
                    if (dict.find(w) != dict.end()) {   // w is in dict
                        // w k output(code of w) index symbol
                        line = w+" "+k+" "+to_string(dict.find(w)->second);
                    } else {    // w is not in dict
                        // w k ouput index symbol
                        line = w+" "+k+" "+w;
                    }
                }
                w.clear();
                w.push_back(k); // w = k
            } else {
                // w k blank blank blank
                line = w+" "+k;
                w.push_back(k);
            }
            output += line + "\n";
            line.clear();
        }
    }
    if (w.length() > 1) {
        // w EOF output
        line = w + " " + "EOF" + " " + to_string(dict.find(w)->second);
    } else {
        line = w + " " + "EOF" + " " + w;
    }
    output += line;
    // TODO: k is EOF
    return output; 
}

void output_codewords(const vector<unsigned int> &codes) {
     for(vector<unsigned int>::const_iterator cw = codes.begin(); cw!=codes.end(); cw++){
        if (cw < codes.end()-1) {
            if (*cw < 256 || *cw > 16384) {
                printf("%c ", *cw);
            } else {
                printf("%u ", *cw);
            }
        } else {
            if (*cw < 256 || *cw > 16384) {
                printf("%c", *cw);
            } else {
                printf("%u", *cw);
            }
        }
    }
    printf("\n");   
}

int main(int argc, char* argv[]) {
    string input;
    string params;
    bool error = false;
    vector<unsigned int> codes;
    input = read_stdin();
    if (argc == 2) {
        params = argv[1];
        if (params.length() != 2 || (params != "-l" && params != "-d")) {
            // wrong parameter
            cerr << "Usage: lencode [-l]" << endl;
        } else if (params == "-l"){
            // print list
            string list = encode_with_list(input);
            cout << list << endl;
        } else {
            cout << input << endl;
        }
    } else if (argc == 1) {
        codes = encode(input);
        output_codewords(codes);
    } else {
        cerr << "Usage: lencode [-l]" << endl;
    }

    return 0;
}