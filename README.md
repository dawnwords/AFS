Simple Andrew File System
=========================

1 Components
----------
I implemented a simple version of Andrew File System using Java programing language. 

The main components for my AFS are Vice Server and Venus Client. They communicated with each other using Java RMI, remote method invocation, which is the Java version of Remote Procedure Call. Vice Server provides register, fetch, store, create, makeDir, remove, setLock, releaseLock and removeCallback, totally 9 remote methods for Venus to invoke while Venus provides breakCallback for Vice to callback. RMI encapsulates the data object transmission which simplifies the coding tasks to some degree.

Venus Client is wrapped with a command-line user interface providing ls, cd, mkdir, touch and rm 5 directory operations and open, read, write, close 4 file operations for users.

2 Technical Detail
----------------
##2.1 Connection Setup##
When Vice Server is started, it first initialize a file standing for the ROOT directory with fid 0000000000000000ffffffff if this is the first time for server starting. Then it creates an empty list of Locks used to holding the locks for each file and an empty map between client id, a long number generated based on the hash code of client RMI callback URI string, and the delegate of client RMI callback stub. After that, it scans the whole Vice Directory containing all the files to get the max number of uniquifier that has been used to get current uniquifier. Then it begins to wait for clients.
When a Venus client starts, it firstly invokes the remote call of register to register its RMI callback URI at sever. The register method do the following jobs: generating the delegate of client RMI callback stub, generate the hash code of the given URI as client id and put the mapping of the client id and the reference of client delegate into the map mentioned in the former paragraph. 
After that the connection between client and server is set up.

##2.2 Eight Main Components of Vice Service Interfaces##
**create & makedir**
> Create a file with a given FID by client which is easy to implement.

**fetch & store**
> Read and write data as byte array into the file with given FID. Easy to implement.

**remove**
> Use a recursive method to remove the file with given FID. If it is a directory, perform the same method to each of its child files. If it is a normal file, just remove it. After that, call removeCallback for each client holding the callback promise to inform them that the file has changed. The next time clients wants to visit this file or directory, it asked server for the file since the callback promise is cancelled. And server return a null value indicates that the file has been removed. Then client removes this file with the same fid.

**setLock & realeaseLock**
> A Lock class is in need which encapsulates FID, user id, request time and lock mode. When Venus call setLock, Vice firstly check the list of lock to see if there has already been a lock to the given fid. During traversing, Vice remove the expired Lock object. There are two types of Lock, exclusive one which means no more sharing and shared one which means read-only sharing. If the Lock object with the same FID is found and its lock mode is conflict with the request one, then reject this lock request. If no lock confliction, then add a new Lock Object standing for this request into the lock list. Removing lock is easy to implement. Just traverse the list to remove the lock object.

**removeCallback & breakCallback**
> Callback promises are stored with the cached files on the workstation disks and have two states: valid or cancelled. When a server performs a request to update a file it notifies all of the Venus processes to which it has issued callback promises by sending a callback to each – a callback is a remote procedure call from a server to a Venus process. When the Venus process receives a callback, it sets the callback promise token for the relevant file as cancelled.
> In my design, Venus uses a file named “.cp” to store all the mapping between FID and callback promise state (Vaild or Cancelled). Vice uses a file named “[fid].cp” where fid is the corresponding file this callback promise guarded. This file stores the id of user who has the cache of this file with the corresponding fid. The data structure of those two kind of file will be addressed in next Section.

##2.3 Venus Client Implementation##
Based on the data structure of directory, which I will introduce in the next Section, directory operations are easy to implement. Touch is to invoke remote method create to get an FID and add it as a file item in the directory file then upload the directory file. Cd is to search all the file item of current directory to find the FID of the given directory name and set it as the current one. If the name is “..”, then get the parent directory fid from the file attributes part of current directory and set it as the current one. Ls is to check the current directory and get all its file items then print their name. Rm has a small problem since client may not have all the files stored in nested directory, but as the Vice Interface implemented, the remote method of remove is suitable for a directory, then the problem is solved.
As for other file operations, such as open, read, write and close, I drew a sequence diagram among command line, Venus and Vice to represent their interactions.
![image](https://github.com/dawnwords/AFS/raw/master/pic/fileOperation.jpg)

##2.4 Security of Data Transmission##
Since asymmetric encryption of data is used for encrypting data shorter than the key which is hard to use for large data transmission and the key passing logic is hard to implement, I choose to use Java symmetric encryption with the key embedded in the source code for file data transmission.

##2.5 Log##
There is a singleton class Log takes the responsibility logging the interactions of Venus clients with the Vice server. After each remote method is invoked at server side, I can get the instance of Log class and call the method i(String format, Object … args) to write down a log item.

3 Data Structures
---------------
##3.1 FID: File Identifier##
Each file and directory in the shared file space is identified by a unique, 96-bit file identifier (FID). In my design, I use first 8 bytes to represent user identification generated as the 64-bit hash code of their RMI callback URI string. The rest 4 bytes are an integer generated by Vice Server as the uniquifier mentioned in the text book which increases as the number of files and directories. To identify whether this FID referred to a file or directory, a negative uniquifier refers to a directory while a positive one refers to a normal file.
![image](https://github.com/dawnwords/AFS/raw/master/pic/FID.png)

##3.2 Files and Directories##
In my design of Simple AFS, each file or directory are stored as a single file above file system on OS. File attributes are stored at the beginning of each file, occupying 64 bytes. Those attributes includes creating time, modifying time, creator id, file size and the fid of its parent directory.
![image](https://github.com/dawnwords/AFS/raw/master/pic/fileStructure.png)
The above figure shows the structure of a normal file. From the 65-th byte, the file content are transferred and stored as bytes. The following figure shows the structure of a directory. Different from normal file, each directory should contain all the information about the names and fids of its child files. The file name of each child file occupies 52 bytes, which allows only 52 ascii character as files name, while its FID fills the rest 12 bytes.
![image](https://github.com/dawnwords/AFS/raw/master/pic/directoryStructure.png)

##3.3 Callback Promise Files##
As I mentioned at the former section, Venus uses one file to maintain all callback promises of FIDs. I use one byte to stands for whether the callback promise is valid. Then the “.cp” file for Venus may looks as the following.
![image](https://github.com/dawnwords/AFS/raw/master/pic/cp.png)

Since Vice maintains callback promises for each file, then structure of each “[fid].cp” file are shown below.
![image](https://github.com/dawnwords/AFS/raw/master/pic/servercp.png)

Pros and Cons for This AFS Implementation
-----------------------------------------
My AFS Implementation works well when the server and all clients are work correctly. The cache files of client maintained by callback promises does work so that all clients can notice the modification of others.
Some shortcomings for my system is that the crash recovery is not concerned as the limit of time. There is no GUI for Venus, but my implementation of Venus is not coupled with commands of line command. It can be easily expanded with Java GUI components. If there is more time, I may try to finish it.

**A Bug Remained**
If Client A goes deep into a nested folder by using cd command and Client B removed an ancestor folder and A will be stuck there since it cannot perform “cd ..”. Thus I add “cd /” for such case to help A return to the root folder. And the cache file may not be removed by some special cases but it is not visible to application layer. Given more time, I will try to think up a proper fix to this bug.