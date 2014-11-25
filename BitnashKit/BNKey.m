//
//  BNKey.m
//  BitnashKit
//
//  Created by Rich Collins on 5/1/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import "BNKey.h"

@implementation BNKey

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"pubKey",
                                                   @"address",
                                                   @"creationTime",
                                                   nil]];
    
    {
        NavActionSlot *slot = [self.navMirror newActionSlotWithName:@"inspect"];
        [slot setVisibleName:@"inspect"];
        [slot setIsActive:YES];
        //[slot setVerifyMessage:@""];
    }
    
    return self;
}

- (void)setAddress:(NSString *)address
{
    _address = address;
    //NSLog(@"BNKey setAddress %@", _address);
}

- (NSDate *)creationDate
{
    return [NSDate dateWithTimeIntervalSince1970:[_creationTime longValue]/1000];
}

- (NSString *)nodeTitle
{
    return self.address;
}

- (NSString *)nodeSubtitle
{
    NSString *dateString =  [self.creationDate
                             descriptionWithCalendarFormat:@"%Y-%m-%d %H:%M:%S" timeZone:nil
                             //descriptionWithCalendarFormat:@"%x %X %Z" timeZone:nil
            //descriptionWithCalendarFormat:@"%c" timeZone:nil
            locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
    return [@"Created " stringByAppendingString:dateString];
}

- (NSUInteger)hash
{
    return [self.address hash];
}

- (BOOL)isEqual:(id)object
{
    if ([object respondsToSelector:@selector(address)])
    {
        return [self.address isEqual:[object performSelector:@selector(address)]];
    }
    
    return NO;
}

- (BOOL)isEqualTo:(id)object
{
    return [self isEqual:object];
}

// --------------------

- (NSString *)webUrl
{
    return [@"http://testnet.helloblock.io/addresses/" stringByAppendingString:self.address];
}

// actions

- (void)inspect
{
    NSURL *url = [NSURL URLWithString:self.webUrl];
    [[NSWorkspace sharedWorkspace] openURL:url];
}


@end
