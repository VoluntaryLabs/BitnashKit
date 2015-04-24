//
//  BitnashJ.m
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import "BNServer.h"
#import "NSObject+BN.h"

@implementation BNServer

- (id)init
{
    self = [super init];
    self.walletPath = [NSHomeDirectory() stringByAppendingString:@"/.bitnash"];
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
                            [frameworkBundle pathForResource:@"bitcoinj-0.11.3-bundled" ofType:@"jar"],
                            [frameworkBundle pathForResource:@"json-simple-1.1.1" ofType:@"jar"],
                            [frameworkBundle pathForResource:@"slf4j-simple-1.7.7" ofType:@"jar"],
                            nil] componentsJoinedByString:@":"];
    
    self.task = [[NSTask alloc] init];
    _task.currentDirectoryPath = _walletPath;
    _task.launchPath = @"/usr/bin/java"; //TODO: Locate it first?
    
    NSMutableArray *arguments = [NSMutableArray arrayWithObjects:
                                 @"-Dfile.encoding=MacRoman",
                                 @"-classpath", classPath,
                                 @"org.bitmarkets.bitnash.BNApp",
                                 @"-testnet", self.usesTestNet ? @"true" : @"false",
                                 nil];
    
    if (_checkpointsPath)
    {
        [arguments addObjectsFromArray:@[@"-checkpoints", _checkpointsPath]];
    }
    
    if (_torSocksPort)
    {
        [arguments addObjectsFromArray:@[@"-tor-socks-port", [_torSocksPort description]]];
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
    
    if ([[[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding] UTF8String] == nil)
    {
        [NSException raise:@"message contains invalid UTF-8 bytes" format:nil];
    }
    
    //NSLog(@"SENT: %@", [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding]);
    
    [[_task.standardInput fileHandleForWriting] writeData:jsonData];
    [[_task.standardInput fileHandleForWriting] writeData:[@"\n" dataUsingEncoding:NSUTF8StringEncoding]];
    
    
    
    NSMutableData *output = [NSMutableData data];
    int lastByte = 0;
    while (lastByte != 10)
    {
        NSData *data = [[_task.standardOutput fileHandleForReading] availableData];
        [output appendData:data];
        NSRange range;
        range.location = output.length - 1;
        range.length = 1;
        [output getBytes:&lastByte range:range];
    }
    
    
    NSString *json = [[NSString alloc] initWithData:output encoding:NSUTF8StringEncoding];
    
    //NSLog(@"RECEIVED: %@", json);
    
    if (json == nil)
    {
        NSLog(@"Received non-UTF8 string");
        return nil;
    }
    
    NSDictionary *response = [NSJSONSerialization JSONObjectWithData:output options:0x0 error:&error];
    
    if (error)
    {
        [NSException raise:error.description format:nil];
    }
    
    if ([response objectForKey:@"error"])
    {
        //NSString *errorString = [[response objectForKey:@"error"] asObjectFromJSONObject];
        self.error = [[response objectForKey:@"error"] asObjectFromJSONObject];
        NSString *errorName = [self.error description];
        NSLog(@"wallet error [%@]", self.error);
        [NSException raise:errorName format:nil];
        return nil;
    }
    else
    {
        return [[response objectForKey:@"obj"] asObjectFromJSONObject];
    }
}

- (NSString *)ping:(NSString *)data
{
    return [self sendMessage:@"ping" withObject:self withArg:data];
}

@end
