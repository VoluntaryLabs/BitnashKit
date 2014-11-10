//
//  BNJavaInstall.m
//  BitnashKit
//
//  Created by Steve Dekorte on 11/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNJavaInstall.h"

@implementation BNJavaInstall

- (NSString *)javaExePath
{
    return @"/usr/bin/java";
}

- (BOOL)isJavaInstalled
{
    NSTask *task = [[NSTask alloc] init];
    [task setLaunchPath:self.javaExePath];
    
    NSPipe *outPipe = [NSPipe pipe];
    
    NSFileHandle *nullDevice = [NSFileHandle fileHandleWithNullDevice];
    [task setStandardInput: nullDevice];
    [task setStandardOutput:[outPipe fileHandleForWriting]];
    [task setStandardError:nullDevice];
    
    //NSMutableArray *args = [NSMutableArray array];
    //[task setArguments:args];
    [task launch];
    [task waitUntilExit];
    
    NSData *theData = [outPipe fileHandleForReading].availableData;
    NSString *result = [[NSString alloc] initWithData:theData encoding:NSUTF8StringEncoding];
    
    return ![result hasPrefix:@"No Java runtime"];
}

- (void)presentInstaller
{
    NSTask *task = [[NSTask alloc] init];
    [task setLaunchPath:self.javaExePath];
    [task launch];
}

@end
