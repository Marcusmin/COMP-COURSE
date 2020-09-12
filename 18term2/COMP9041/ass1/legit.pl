#!/usr/bin/perl -w

#  .legit:
#      .temp:(index)
#         files...(files in index)
#      log(record the master's commit's comments)
#      current_branch_log(record current branch's name)
#      .branches:(store the commit of branches)
#         .branch_name_1...:
#            log
#            .snapshot.n:
#               files...
#      .repository:(store the commit of all branches)
#         .snapshot.n...:
#            files...

#create a repo for next operation
use File::Copy;
use File::Compare;
use File::Path;

sub init{
   my $repo_name = ".legit";
   my $index = ".temp";
   my $commit_repo = ".repository";
   my $branch_folders = ".branches";
   if(-e $repo_name){   #if repository has been exist, remind usr.
      print "legit.pl: error: .legit already exists\n";
   }else{
      mkdir $repo_name or die "Create repo failed\n"; #create a hidden folder
      my $log = "log.txt"; 
      open my $f, '<', "$repo_name/$log"; #create log file
      open my $log_file, '>', "$repo_name/current_branch_log";#create a log file to record current branch
      print $log_file "master\n";  #default branhc when init is 'master'
      mkdir "$repo_name/$branch_folders" or die "cannot create $branch_folders:$!\n";
      mkdir "$repo_name/$branch_folders/master" or die "cannot create master:$!\n";#create a branch
      open my $branch_log,'>',"$repo_name/$branch_folders/master/log" or die "$!\n";
      mkdir "$repo_name/$index" or die;   #create index folder for add
      mkdir "$repo_name/$commit_repo" or die;   #create repository for committed files
      print "Initialized empty legit repository in .legit\n";
   }
}
#add more or more file in sub-dir of .legit
sub add{
   my ($command, @files) = @_;
   my $repo = ".legit";
   if(! -e $repo){
      die "legit.pl: error: no .legit directory containing legit repository exists\n"; #if repository has not been created, initial repository first
   }else{
      my $index = "$repo/.temp";
      for $f (@files){
         if(-e $f && $f =~ /^[a-z0-9]/i && $f =~ /^[0-9a-z.-_]+$/i){ #the name of file should be legal
            copy $f, $index;  #copy the file to index folder
         }else{
            if ( (!-e $f) && (-e "$index/$f")){
               unlink "$index/$f";
               return 0;
            } #if file not in local, but it in index, then remove the file from index
            print "legit.pl: error: can not open '$f'\n";   #if file is not exist or name is illegal, remind user.
            return 0;
         }
      }
   }
}

sub commit{
   if(@_ == 4){   #the command combined "add" and "commit"
      my ($command, $option_1, $option_2, $comment) = @_;
      my $repo = ".legit";
      my $index = "$repo/.temp";
      my @files_index = glob "$index/*";  #acquire files in index(candidate for commit)
      for my $f (@files_index){
         $f =~ s/.+\///ig; #eliminate the prefix of file
      }
      add("add", @files_index);  #causes all files already in the index to have their contents from the current directory
      commit($command, $option_2, $comment); #then commit
   }elsif(@_ == 3){  #this command only need to commit the file which has already been added to index folder
      my ($command, $option, $comment) = @_;
      my $repo = ".legit";
      my $index = "$repo/.temp";
      my $committed_repo = "$repo/.repository";
      my $branch_folders = ".branches";
      if(-e $index){ #if index folder has already been exist
         if(! -e $repo){   #this check maybe useless
            die "Initialize an legit repository first!\n";
         }else{
            my $snapshot = "$committed_repo/.snapshot";
            my $counter = 0;
            while(-e "$snapshot.$counter"){
               $counter++;
            }  #find out what name the snapshot should be
            $preversion_counter = $counter - 1;
            $previous_snapshot = "$snapshot.$preversion_counter";
            #if previous snapshot is -1, which means this check is no effect
            my @files = glob "$index/*";
            my @pre_files = glob "$previous_snapshot/*";
            #modify the file name
            for my $e (@files){
               $e =~ s/$index\///;
            }
            for my $e (@pre_files){
               $e =~ s/$previous_snapshot\///;
            }
            if($preversion_counter >= 0){
               if(@files != @pre_files){  #if two folder is different, allow to commit
                  ;
               }else{   #else, they have the same number of files, check whether these files are same
                  $flag = @files;   #the flag is the size of the folder
                  for my $e (@files){
                     #print "$index/$e   $previous_snapshot/$e";
                     if((-e "$index/$e") && (-e "$previous_snapshot/$e")){  #if files in two folder both exist, then check their content
                        if((-z "$index/$e") xor (-z "$previous_snapshot/$e")){ #if file in two folder is not both empty, then allow user to commit
                           ;
                        }else{
                           if ((-z "$index/$e") && (-z "$previous_snapshot/$e")){
                              $flag --;   #two same empty file
                           }else{
                              open my $index_f,'<', "$index/$e";
                              open my $pre_snapshot_f, '<', "$previous_snapshot/$e";
                              my $index_f_content;
                              my $pre_snapshot_f_content;
                              while(my $line_1 = <$index_f>){
                                 $index_f_content .= $line_1;
                              }
                              while(my $line_2 = <$pre_snapshot_f>){
                                 $pre_snapshot_f_content .= $line_2;
                              }
                              if ($index_f_content eq $pre_snapshot_f_content){
                                 $flag --;
                              }
                           }
                        }
                     }else{   #otherwise, allow to commit
                        ;
                     }
                  }
                  if($flag == 0){   #if flag is zero, means all files in two folders are the same
                     print "nothing to commit\n";
                     return 0;
                  }
               }
            }
            #otherwise copy the files in index folder to repository's snapshot
            $snapshot = "$snapshot.$counter";
            mkdir $snapshot or die "$!";
            for my $f (@files){
               copy "$index/$f", $snapshot;
            }
            #if this is first commit, create a master branch
            if($counter == 0){
               mkdir "$repo/$branch_folders/master";
               open my $b_log,'>', "$repo/current_branch_log";
               print $b_log "master\n";
            }
            print "Committed as commit $counter\n";

            #find out on which branch user is
            open my $b_log, '<', "$repo/current_branch_log";
            my $cur_branch = <$b_log>;
            chomp $cur_branch;
            #have a copy of commit to its branch folder
            if(! $cur_branch eq "master"){
               if (! -e "$repo/$branch_folders/$cur_branch"){
                  mkdir "$repo/$branch_folders/$cur_branch";
               }
               if (! -e "$repo/$branch_folders/$cur_branch/.snapshot.$counter"){
                  mkdir "$repo/$branch_folders/$cur_branch/.snapshot.$counter";
               }
               for my $f (@files){
                  copy "$index/$f", "$repo/$branch_folders/$cur_branch/.snapshot.$counter";
               }  
               open my $log, '>>', "$repo/$branch_folders/$cur_branch/log" or die "$!"; #write the comment of commit to log file
               print $log "$counter $comment\n";
            }else{
               if (! -e "$repo/$branch_folders/$cur_branch/$snapshot"){
                  mkdir "$repo/$branch_folders/$cur_branch/.snapshot.$counter";
               }
               for my $f (@files){
                  copy "$index/$f", "$repo/$branch_folders/$cur_branch/.snapshot.$counter";  #also commit a copy to its branch folder  
               }
               open my $log, '>>', "$repo/log" or die "$!"; #write the comment of commit to log file
               print $log "$counter $comment\n";
            }
         }
      }
   }else{
      #if the number of command argument is not correct, die
      die "Too many command!\n";
   }
}

sub log_show{
   my $repo = ".legit";
   my $index = ".temp";
   my $committed_repo = "$repo/.repository";
   my $first_snapshot = "$committed_repo/.snapshot.0";
   my @content;
   if(! -e $first_snapshot){  #cannot show if there is no commit yet
      print "legit.pl: error: your repository does not have any commits yet\n";
      return 0;
   }
   open my $log, '<', "$repo/log";
   if(eof $log){  #if log is empty, remind user
      print "Your log file is empty\n";
      return 0;
   }
   while(my $line = <$log>){
      push @content, $line;
   }
   while(@content){
      print pop @content;
   }
}

sub show{
   my ($command, $file) = @ARGV;
   my $repo = ".legit";
   my $index = ".temp";
   my $committed_repo = "$repo/.repository";
   my $first_snapshot = "$committed_repo/.snapshot.0";
   my @file = split /:/, $file;  #separate specified file and its version
   $file = $file[1];
   my $version = $file[0];
   if(! -e $first_snapshot){  #if usr has not committed yet, invaild command
      print "legit.pl: error: your repository does not have any commits yet\n";
      return 0;
   }
   if(!defined $version && !defined $file){  #invalid argument
      print "You are not required to detect this error or produce this error message.\n";
      exit 1;
   }
   if($version eq ""){
      if(! -e "$repo/$index/$file"){   #file may not add yet
         print "legit.pl: error: '$file' not found in index\n";
         return 0;
      }
      open my $f, '<', "$repo/$index/$file";
      while(my $line = <$f>){ #if version is empty, legit has to show usr its file in index
         print $line;
      }
   }else{
      if(! $version =~ /[^0-9]+/){  #if version is not a number, remind user
         print "legit.pl: error: unknown commit '$version'\n";
         return 0;
      }else{
         my $snapshot = ".snapshot.$version";   #specified snapshot(commited version)
         my $path_of_file = "$committed_repo/$snapshot/$file";
         if(-e $path_of_file){
            open my $f, '<', $path_of_file;
            while(my $line = <$f>){
               print "$line";
            }
         }else{   #if the file is not exist
            if(! -e "$committed_repo/$snapshot"){  #if the version of committed is not exist, remind user
               print "legit.pl: error: unknown commit '$version'\n";
               return 0;
            }else{   #otherwise, the files is not commited yet
               print "legit.pl: error: '$file' not found in commit $version\n";
               return 0;
            }
         }
      }
   }
}
#common remove command. It should remove the file from current directory and index
sub common_rm{
   my ($command, @files) = @_;
   my $repo = ".legit";
   my $index = "$repo/.temp";
   my $committed_repo = "$repo/.repository";
   my $snapshot = "$committed_repo/.snapshot";
   my $counter = 0;
   my $first_snapshot = "$committed_repo/.snapshot.0";
   #check if usr committe at least onece
   if(! -e $first_snapshot){
      print "legit.pl: error: your repository does not have any commits yet\n";
      return 0;
   }
   while(-e "$snapshot\.$counter"){
      $counter++;
   }
   $pre_counter = $counter - 1;
   #give an error message instead of removing the file in the current directory if it is different to the last commit####
	if($pre_counter >= 0){
   	for my $e (@files){
      	if(-e "$index/$e" xor -e "$snapshot.$pre_counter/$e"){	#one of the folder don't have the files
      	   print "legit.pl: error: '$e' has changes staged in the index\n";
      	   return;
      	}elsif(-e "$index/$e" && -e "$snapshot.$pre_counter/$e" ){	#files have different content in index and repo
				if (compare("$index/$e", "$snapshot.$pre_counter/$e") != 0 && compare("$index/$e", "$e") != 0){
					print "legit.pl: error: 'b' in index is different to both working file and repository\n";
					return 0;
				}#legit.pl: error: 'b' in index is different to both working file and repository
				if(compare("$index/$e", "$snapshot.$pre_counter/$e") != 0){
					print "legit.pl: error: '$e' has changes staged in the index\n";	#raise problem
      	   	return;
				}
			}
   	}
   	for my $e (@files){
      	if(-e $e && ! -e "$snapshot.$pre_counter/$e"){	#if the files exist in local, but not in repo
      	   print "legit.pl: error: '$e' is not in the legit repository\n";	#raise problem
      	   return;
      	}
			if(-e $e && -e "$snapshot.$pre_counter/$e"){
				if(compare("$e", "$snapshot.$pre_counter/$e") != 0){
					print "legit.pl: error: '$e' in repository is different to working file\n";
					return;
				}
			}
   	}
	}
   for my $f (@files){
      unlink $f;  #remove from current directory
   }
   for my $f (@files){
      unlink "$index/$f";  #remove from index directory
   }  
}
sub remove_cached{
   my ($command, $option_1, $option_2, @files) = @_;
   my $repo = ".legit";
   my $index = "$repo/.temp";
   my $committed_repo = "$repo/.repository";
   my $snapshot = "$committed_repo/.snapshot";
   my $first_snapshot = "$committed_repo/.snapshot.0";
   my $counter = 0;
   if(! -e $first_snapshot){
      print "legit.pl: error: your repository does not have any commits yet\n";
      return 0;
   }
   #if files in index is different to both working file and repository, raise warning
   while(-e "$snapshot\.$counter"){
      $counter++;
   }
   $pre_counter = $counter - 1;
   for my $f (@files){
      unlink "$index/$f";  #remove from index folder only 
   }
}

#the file is removed only from the index and not from the current directory. 
sub cached_rm{
   my ($command, $option, @files) = @_;
   my $repo = ".legit";
   my $index = "$repo/.temp";
   my $committed_repo = "$repo/.repository";
   my $snapshot = "$committed_repo/.snapshot";
   my $first_snapshot = "$committed_repo/.snapshot.0";
   my $counter = 0;
   #check if usr committe at least onece
   if(! -e $first_snapshot){
      print "legit.pl: error: your repository does not have any commits yet\n";
      return 0;
   }
   #if files in index is different to both working file and repository, raise warning
   while(-e "$snapshot\.$counter"){
      $counter++;
   }
   $pre_counter = $counter - 1;
   for my $f (@files){
      if(-e $f && -e "$snapshot.$pre_counter/$f" && -e "$index/$f"){
         if (compare("$index/$f", "$snapshot.$pre_counter/$f") != 0 && compare("$index/$f", "$f") != 0){
            print "legit.pl: error: '$f' in index is different to both working file and repository\n";
		      return 0;
         }
      }elsif(! -e "$snapshot.$pre_counter/$f" && ! -e "$index/$f"){
         print "legit.pl: error: '$f' is not in the legit repository\n";
         return;
      } #file is not in the legit repository
   }
   for my $f (@files){
      unlink "$index/$f";  #remove from index folder only 
   }
}

sub force_rm{
   my ($command, $option, @files) = @_;
   my $repo = ".legit";
   my $index = "$repo/.temp";
   my $committed_repo = "$repo/.repository";
   my $snapshot = "$committed_repo/.snapshot";
   my $first_snapshot = "$committed_repo/.snapshot.0";
   my $counter = 0;
   #check if usr committe at least onece
   if(! -e $first_snapshot){
      print "legit.pl: error: your repository does not have any commits yet\n";
      return 0;
   }
   while(-e "$snapshot\.$counter"){
      $counter++;
   }
   $pre_counter = $counter - 1;
   for my $f (@files){
      if(! -e "$index/$f"){  #file is not in the legit repository
         print "legit.pl: error: '$f' is not in the legit repository\n";
      }else{
         unlink "$index/$f";  #remove from index directory
         unlink $f;  #remove from current directory
      } 
   }
}

sub status{
   #files not in snapshot and not in index: untracked
   #files not in snapshot but in index: added to index
   #files not in local but in index and snapshot : file deleted
   #files not in index neither in in local dir, but in snapshot : deleted  
   #file is different in local, index and snapshot:file changed, different changes staged for commit
   #file in local is different with file in snapshot but same with file in index: file changed, changes staged for commit
   #file in local is different with files in snapshot and index: file changed, changes not staged for commit
   my $repo = ".legit";
   my $index = "$repo/.temp";
   my $committed_repo = "$repo/.repository";
   my $snapshot = "$committed_repo/.snapshot";
   my $first_snapshot = "$committed_repo/.snapshot.0";
   my $counter = 0;
   my $f;
   #check if usr committe at least onece
   if(! -e $first_snapshot){
      print "legit.pl: error: your repository does not have any commits yet\n";
      return 0;
   }
   while(-e "$snapshot.$counter"){
      $counter++;
   }
   $pre_counter = $counter - 1;
   #preparation
   #get file name in snapshot
   for $f (glob "$snapshot.$pre_counter/*"){
      $f =~ s/$snapshot\.$pre_counter\///;
      $filename{$f} = 0;
   }
   for $f (glob "$index/*"){
      $f =~ s/$index\///;
      $filename{$f} = 0;
   }
   for $f (glob "./*"){
      $f =~ s/\.\///;
      $filename{$f} = 0;
   }
   for $f (sort keys %filename){
      if(! -e "$index/$f"){
         if(-e "$snapshot.$pre_counter/$f" && ! -e "$f"){  #if file only be in snapshot, then file: deleted
            print "$f - deleted\n";
         }else{   #if file not in index neither in snapshot, then file is untracked
            print "$f - untracked\n";
         }
      }elsif(-e "$index/$f" && -e "$snapshot.$pre_counter/$f" && ! -e "$f"){
         print "$f - file deleted\n";
      }elsif(! -e "$snapshot.$pre_counter/$f" && -e "$index/$f" && -e "$f"){ #files not in snapshot yet, but in index and in local dir
         print "$f - added to index\n";
      }elsif(compare("$index/$f", $f) != 0 && compare("$index/$f", "$snapshot.$pre_counter/$f") == 0 && compare("$f", "$snapshot.$pre_counter/$f") != 0){
         print "$f - file changed, changes not staged for commit\n";
      }elsif(compare("$index/$f", $f) == 0 && compare($f, "$snapshot.$pre_counter/$f") != 0){
         print "$f - file changed, changes staged for commit\n";
      }elsif(compare("$index/$f", $f) != 0 && compare("$index/$f", "$snapshot.$pre_counter/$f") != 0){
         print "$f - file changed, different changes staged for commit\n";
      }elsif(compare("$index/$f", "$snapshot.$pre_counter/$f") == 0 && compare("$index/$f", $f) == 0){
         print "$f - same as repo\n";
      }
   }
}

sub create_branch{
   #branch command
   my $repo = ".legit";
   my $index = "$repo/.temp";
   my $committed_repo = "$repo/.repository";
   my $snapshot = "$committed_repo/.snapshot";
   my $first_snapshot = "$committed_repo/.snapshot.0";
   my $counter = 0;
   my $branch_folders = ".branches";
   if(! -e $first_snapshot){
      print "legit.pl: error: your repository does not have any commits yet\n";
      return;
   }
   #create a new hidden folder in .legit, which has the last commmit of master branch
   #create a branch log
   if(@ARGV >= 2 && $ARGV[1] eq "-d"){   #Going to delete the branch
      my ($command, $option, @files) = @ARGV;
      for my $f (@files){
         if($f eq "master"){
            print "legit.pl: error: can not delete branch 'master'\n";
            next;
         }else{
            if(! -e "$repo/$branch_folders/$f"){
               print "legit.pl: error: branch '$f' does not exist\n";
            }else{
               rmtree "$repo/$branch_folders/$f" or die "$!\n";
               print "Deleted branch '$f'\n";
            }
         }

      }
   }else{   #otherwise, create a branch or print out branch's name
      if(@ARGV == 1){   #print branch's name
         	for my $f (glob "$repo/$branch_folders/*"){
               $f =~ s/$repo\/$branch_folders\///;
               print "$f\n";
            }
      }else{   #create a series of new folders for snapshot of files
        my ($command, @branches) = @ARGV;
         # while(-e "$snapshot.$counter"){
      	#   $counter++;
   		# }
   		# $pre_counter = $counter - 1;
         mkdir "$repo/$branch_folders";
         for my $b (@branches){
            if( -e "$repo/$branch_folders/$b"){
               print "legit.pl: error: branch '$b' already exists\n";
               return ;
            }
            mkdir "$repo/$branch_folders/$b" or die "Cannot create $b :$!";
            mkdir "$repo/$branch_folders/$b/index" or die "$!\n";
            mkdir "$repo/$branch_folders/$b/working_directory" or die "$!\n";
            #make a snapshot of working directory and index
            # for my $f (glob "$committed_repo/$snapshot.pre_counter/*"){
            #    copy $f, "$repo/$branch_folders/$b";   #copy the files to branch's folders
            # }
            #snapshot the working directory
            for my $f (glob("./*")){
               copy $f, "$repo/$branch_folders/$b/working_directory";
            }
            #copy index snapshot
            for my $f (glob("$index/*")){
               copy $f, "$repo/$branch_folders/$b/index";
            }
            open my $log, '<', "$repo/$branch_folders/$b/log"; #create a log file to record the branch's commit
         }
         #also create a master branch
         #default branch is master
      }
   }
}

sub checkout{
   #checkout command is to switch to another branch
   #modify the branch_log to current branch
   my ($command, $branch_name) = @ARGV;  #accept arguments
   my $repo = ".legit";
   my $index = "$repo/.temp";
   my $committed_repo = "$repo/.repository";
   my $snapshot = "$committed_repo/.snapshot";
   my $first_snapshot = "$committed_repo/.snapshot.0";
   my $counter = 0;
   my $branch_folders = ".branches";
   #get branches' name
   for $b (glob("$repo/$branch_folders/*")){
      $b =~ s/$repo\/$branch_folders\///;
      $branches{$b} = 0;
   }
   if(!defined $branches{$branch_name}){ #if the branch not exist
      print "legit.pl: error: unknown branch '$branch_name'\n";
      return;
   }else{
      #check if current working directory and index has same files as repository
      #remove the non-exist files
      my @folder_in_repo = glob("$repo/$branch_folders/$branch_name/\.*");
      for $i (@folder_in_repo){
            $i =~ s/$repo\/$branch_folders\/$branch_name\///;
            if($i =~ /\.snapshot\./){
                  push @snapshot_in_repo, $i;
            }
      }
      if(@snapshot_in_repo != 0){
         my $last_snapshot = pop @snapshot_in_repo;
         my @file_in_repo = glob("$repo/$branch_folders/$branch_name/$last_snapshot/*");
         #copy the branch's commit file into working directory
         for my $e (@file_in_repo){
            $e =~ s/$repo\/$branch_folders\/$branch_name\/$last_snapshot\///;
            $file_in_repo{$e} = 0;
            copy "$repo/$branch_folders/$branch_name/$last_snapshot/$e", "./$e";
         }
         #if file in working dir is not exist in last commit, remove it
         for my $e (glob("./*")){
            $e =~ s/\.\///;
            if(! defined $file_in_repo{$e}){ 
               unlink $e;
            }
         }
      }else{
         #if this branch has no commit yet
         #if there is a file which not in snapshot of branch, remove them
         for my $i (glob("$repo/$branch_folders/$branch_name/working_directory/*")){
            $i =~ s/$repo\/$branch_folders\/$branch_name\/working_directory\///;
            copy "$repo/$branch_folders/$branch_name/working_directory/$i", $i or die "$!\n";
            $branch_wd_file{$i} = 0;
         }
         for my $i (glob("./*")){
            $i =~ s/\.\///;
            if(!defined $branch_wd_file{$i}){
               unlink $i;
            }
         }
      }

      open my $f, '<', "$repo/current_branch_log";
      $current_branch = <$f>;
      chomp $current_branch;
      if($current_branch eq $branch_name){
         print "Already on '$branch_name'\n";
         return;
      }
      print "Switched to branch '$branch_name'\n";
      #write into log about where branch is 
      open $f, '>', "$repo/current_branch_log"; 
      print $f "$branch_name\n";
      return;
   }
}
$unknown_msg = "Usage: legit.pl <command> [<args>]\n
These are the legit commands:
   init       Create an empty legit repository
   add        Add file contents to the index
   commit     Record changes to the repository
   log        Show commit log
   show       Show file at particular state
   rm         Remove files from the current directory and from the index
   status     Show the status of files in the current directory, index, and repository
   branch     list, create or delete a branch
   checkout   Switch branches or restore current directory files
   merge      Join two development histories together\n";
if(@ARGV <= 0){
   print $unknown_msg;
}elsif(lc $ARGV[0] eq "init"){
   init(@ARGV);
}elsif(lc $ARGV[0] eq "add"){
   add(@ARGV);
}elsif(lc $ARGV[0] eq "commit"){
   commit(@ARGV);
}elsif(lc $ARGV[0] eq "log"){
   log_show(@ARGV);
}elsif(lc $ARGV[0] eq "show"){
   if(@ARGV != 2){
      print "usage: legit.pl <commit>:<filename>\n";
      exit 0;
   }
   show(@ARGV);
}elsif(lc $ARGV[0] eq "rm"){
   if(@ARGV  >= 4 && ($ARGV[1] eq "--force" && $ARGV[2] eq "--cached" || $ARGV[2] eq "--force" && $ARGV[1] eq "--cached")){
      remove_cached(@ARGV);
   }elsif($ARGV[1] eq "--force"){
      force_rm(@ARGV);
   }elsif($ARGV[1] eq "--cached"){
      cached_rm(@ARGV);
   }else{
      common_rm(@ARGV);
   }
}elsif(lc $ARGV[0] eq "status"){
   status(@ARGV);
}elsif(lc $ARGV[0] eq "branch"){
   create_branch(@ARGV);
}elsif(lc $ARGV[0] eq "checkout"){
   checkout(@ARGV);
}else{
      print "legit.pl: error: unknown command $ARGV[0]";
      print "$unknown_msg";
}
