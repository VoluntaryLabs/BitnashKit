//
//  BitnashJ.m
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNServer.h"
#import "NSObject+BN.h"

@implementation BNServer

- (id)init
{
    self = [super init];
    self.walletPath = [NSHomeDirectory() stringByAppendingString:@"/.bitnash"];
    self.logs = YES;
    return self;
}

/*
- (void)watchTaskStandardError
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(taskStandardErrorNotified:) name:NSFileHandleReadCompletionNotification object:self.taskStandardError];
    [self.taskStandardError readInBackgroundAndNotify];
}

- (void)taskStandardErrorNotified:(NSNotification *)notification
{
    NSData *data = [notification.userInfo objectForKey:NSFileHandleNotificationDataItem];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:NSFileHandleReadCompletionNotification object:self.taskStandardError];
    if (data)
    {
        if (self.logs)
        {
            NSString *filePath = [self.walletPath stringByAppendingPathComponent:@"bitnash.log"];
            
            if (![[NSFileManager defaultManager] fileExistsAtPath:filePath])
            {
                [[NSFileManager defaultManager] createFileAtPath:filePath contents:[NSData data] attributes:nil];
            }
            NSFileHandle *fileHandle = [NSFileHandle fileHandleForWritingAtPath:filePath];
            [fileHandle seekToEndOfFile];
            [fileHandle writeData:data];
            [fileHandle closeFile];
        }
        [self watchTaskStandardError];
    }
}
 */

- (void)start
{
    if (_started)
    {
        return;
    }
    else
    {
        self.started = YES;
    }
    
    self.error = nil;
    
    [self createDir];
    
    if (self.error)
    {
        return;
    }
    
    NSBundle *frameworkBundle = [NSBundle bundleForClass:self.class];
    
    NSString *classPath = [[NSArray arrayWithObjects:
                            frameworkBundle.resourcePath,
                            [frameworkBundle pathForResource:@"bitcoinj" ofType:@"jar"],
                            [frameworkBundle pathForResource:@"json-simple-1.1.1" ofType:@"jar"],
                            [frameworkBundle pathForResource:@"slf4j-simple-1.7.6" ofType:@"jar"],
                            nil] componentsJoinedByString:@":"];
    
    self.task = [[NSTask alloc] init];
    _task.currentDirectoryPath = _walletPath;
    _task.launchPath = @"/usr/bin/java"; //TODO: Locate it first?
    
    NSMutableArray *arguments = [NSMutableArray arrayWithObjects:
                                 @"-Dfile.encoding=MacRoman",
                                 @"-classpath", classPath,
                                 @"org.bitmarkets.bitnash.BNApp",
                                 nil];
    if (_checkpointsPath)
    {
        [arguments addObject:_checkpointsPath];
    }
    _task.arguments = arguments;
    
    _task.standardInput = [NSPipe pipe];
    _task.standardOutput = [NSPipe pipe];
    
    _task.standardError = [NSPipe pipe];
    //self.taskStandardError = [_task.standardError fileHandleForReading];
    //[self watchTaskStandardError];
    
    [_task launch];
}

- (void)createDir
{
    NSError *error = nil;
    
    if(![[NSFileManager defaultManager] fileExistsAtPath:_walletPath])
    {
        [[NSFileManager defaultManager] createDirectoryAtPath:_walletPath withIntermediateDirectories:YES attributes:nil error:&error];
    }
    
    if (error) {
        BNError *bnError = [[BNError alloc] init];
        bnError.description = [error localizedDescription];
        self.error = bnError;
    }
}

- (id)sendMessage:(NSString *)messageName withObject:(id)object
{
    return [self sendMessage:messageName withObject:object withArg:nil];
}

- (id)sendMessage:(NSString *)messageName withObject:(id)object withArg:(id)arg
{
    if (arg == nil)
    {
        arg = [NSNull null];
    }
    
    if (!_started)
    {
        [self start];
    }
    
    self.error = nil;
    
    NSMutableDictionary *message = [NSMutableDictionary dictionary];
    [message setObject:messageName forKey:@"name"];
    
    if (object == nil)
    {
        object = [NSNull null];
    }
    
    [message setObject:[object asJSONObject] forKey:@"obj"];
    [message setObject:[arg asJSONObject] forKey:@"arg"];
    
    NSError *error = nil;
    
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:message options:0x0 error:&error];
    
    if (error)
    {
        [NSException raise:error.description format:nil];
    }
    
    [[_task.standardInput fileHandleForWriting] writeData:jsonData];
    [[_task.standardInput fileHandleForWriting] writeData:[@"\n" dataUsingEncoding:NSUTF8StringEncoding]];
    
    
    
    NSMutableData *output = [NSMutableData data];
    
    while (![[[NSString alloc] initWithData:output encoding:NSUTF8StringEncoding] hasSuffix:@"\n"])
    {
        NSData *data = [[_task.standardOutput fileHandleForReading] availableData];
        [output appendData:data];
    }
    

    NSDictionary *response = [NSJSONSerialization JSONObjectWithData:output options:0x0 error:&error];
    
    if (error)
    {
        [NSException raise:error.description format:nil];
    }
    
    if ([response objectForKey:@"error"])
    {
        self.error = [[response objectForKey:@"error"] asObjectFromJSONObject];
        [NSException raise:error.description format:nil];
        return nil;
    }
    else
    {
        return [[response objectForKey:@"obj"] asObjectFromJSONObject];
    }
}


@end
