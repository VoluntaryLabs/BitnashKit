//
//  BitnashJ.h
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BNServer : NSObject

@property NSString *path;
//path to folder that will contain the wallet and chainstore.

@property NSError *error;
//Set to most recent error that occured during an operation.

@property BOOL started;

@property BOOL logsStderr;
@property BOOL logsErrors;

@property NSTask *task;

- (void)start;
//Start the wallet child process (BitcoinJ)

- (id)sendMessage:(NSString *)messageName withObject:(id)object;
//send a message to the child and get back an object

@end
