//
//  BitnashJ.h
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNError.h"

@interface BNServer : BNObject

@property NSString *walletPath;
//path to folder that will contain the wallet and chainstore.

@property NSString *checkpointsPath;
//path to the checkpoints file

@property BOOL started;

//set to YES or NO before starting server to enable / disable logging to the wallet log file
@property BOOL logs;

@property NSTask *task;

@property NSFileHandle *taskStandardError;

- (void)start;
//Start the wallet child process (BitcoinJ)

- (id)sendMessage:(NSString *)messageName withObject:(id)object;

- (id)sendMessage:(NSString *)messageName withObject:(id)object withArg:(id)arg;
//send a message to the child and get back an object

@end
